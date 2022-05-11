# インフラリソースを非属人的/安全に管理したい

## インフラ管理の非属人化

インフラリソースを非属人的に管理したい.


理想:


- GitHub あるいはその他 VCS の `*.tf`, 或いはslsなどの他のインフラリソース管理ツール(以下、__IaaC ツール__ と呼ぶ)のファイル群を Single Source of Truth とする(そのためのレポジトリを以下では __IaaC レポジトリ__ と呼ぶ.)
  - インフラリソースの変更は PR のマージ＝承認として扱い、CI/CD から行う
  - ローカルの開発端末・個人のクラウドアカウントからの `terraform apply` の実行を制限する. これはセキュリティ上の理由に加えて、開発者間で同期をとる必要をなくすためである.
  - 開発者は IaaC レポジトリを通してインフラリソースの状態や過去の変更を確認できる.


一方で、すべてを IaaC ツールで管理するのには次のような課題がある.

- IaaC レポジトリの管理/運用が大変になる
- ツールのバグや開発者の理解の浅さが原因で意図せず環境をぶち壊してしまうおそれがある
- inpersonate の準備が複雑化する


### なにを、どこまで IaaC で管理するか

### IaaC の漸進的な導入


## インフラリソースを安全に管理したい

一般的に terraform を実行可能なアカウントにはかなり強い権限が付与されている. terraform が必要な開発者に愚直に terraform の実行が可能な権限を付与すると、開発者が増えるほどインフラ周りのセキュリティのリスクが高まる.

## GCP のセキュリティ管理機能を使う上で重要な概念

### Impersonate(権限の借用)

#### impersonate の何がうれしいか

impersonate しない場合の terraform 運用は次のようなものが考えられる.

0. 全員に owner 権限や editor 権限などの極めて強い権限を付与する.
1. terraform 用の service account を作成し、単一の key を発行する. すべての開発者はその service account の key を共有する.
2. terraform 用の service account を作成し、開発者それぞれにその service account の key を発行する. ある開発者の service account key が漏洩したら, 管理者はその開発者に発行した service account key を無効にする.
3. 必要な時にだけ service account key を admin が新規に発行し、開発者に渡す. 一定時間が経過するか目的が達成されたらその service account key を無効化する.


0, 1 はきわめてずさんな運用である. 開発者が増えれば増えるほど漏洩のリスクが高まる. ある開発者が service account key を漏洩した場合、他のすべての開発者も service account key を変更しなければならない.

2 はマシだが、それでも好ましくない運用である.


そもそも、インフラリソースを操作するような権限は 24 時間 365 日必要なわけではない. 必要な時にだけ権限を付与し、必要な処理が終わったら権限をはく奪するほうが安全である.

3 は、安全性は増えるが、開発者がインフラリソースを操作するたびに admin に権限発行を申請しなければならない. 効率は悪くスケールしない.

Impersonate しない運用(service account key を使う運用)には次のような問題がある.

- service account key は長期間利用できる点. service account key があればだれでも強い権限をもててしまう.
  - 本来24時間365日権限が必要ないユーザーにも期間を制限しないで権限を付与してしまう.

- 開発者が増えれば増えるほど、安全に管理・運用するコストが増える
  - 開発者は増えると発行する service account key も増えるので漏洩のリスクも比例して増える.
  - key は手動で無効化しなければならない. だれかが漏洩した場合、その影響範囲を確かめなければならない. そもそも漏洩したとしても漏洩に気づかなければ対応が遅れる.


逆に次の条件を満たすものがあればうれしい.

- 開発者数に比例して鍵の数が増えない.
- 開発者の権限は最小限になる(閲覧と短命トークンの発行)
- 必要な時にだけ権限が付与され、短期間で権限は自動消滅する(運用が楽、漏洩時の被害を抑えられる)

それが権限借用による運用(impersonate)である.

### Workload Identity Pool

- 外部の Open ID Connect(OIDC) Provider <=>GCPのサービスアカウントのマッピングの集合


#### Workload Identity Pool のなにがうれしいか

GCP の service account key の利用を避けるために インフラリソースを変更可能な service account を terraform とごく限られた admin だけにすることを考えてみよう.

このとき、開発者がインフラリソースを操作したくなったとき
1. admin が `gcloud` コマンドや 管理画面を使って手動で開発者に Token 作成権限を付与する. 
2. 開発者が IAM を管理している `*.tf` ファイルのある IaaC レポジトリに自身に Token 作成権限を付与するような PR を送り、admin は PR のマージによってそれを承認する.

ケースがある.

これ以降、開発者は Token 作成権限がある限り terraform の service account の権限を借用してインフラリソースを操作できる. 運用の自動化の観点では２のほうが望ましいが、2は GitHub Actions 上で terraform の service account の権限が必要なので少なくとも一つ service account key を発行しなければならない.

Workload Identity を使えば、この terraform の service account key を発行する必要も無くなる. terraform のセットアップの手間が減り、また、割れ窓理論ではないが、例外的に service account key を利用するケースを排除できてうれしい.


### Workload Identity Provider

- GCP,あるいは OICD Consumer は,(外部の)Provider(e.g. GitHub)が ID(これは具体的なユーザーに限らない識別子) を適切に管理していると仮定している.
- Provider の管理するID(e.g. ある組織のあるレポジトリのGitHub Actionsを実行するAgentの識別子) と、 roles/iam.workloadIdentityUser の role をもつ仮想的なメンバーを紐づける
    ```sh
    gcloud iam service-accounts add-iam-policy-binding "${SERVICE_ACCOUNT}" \
  --project="${PROJECT_ID}" \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/${WORKLOAD_IDENTITY_POOL_ID}/attribute.repository/${GitHubユーザー名}/${リポジトリ名}"
    ```
- Provider は ID のもとで  SERVICE_ACCOUNT(e.g. terraform を実行するための service account) の権限を借用するためにリクエストを送る

- GCP 側はリクエストが
  - 正規の Provider から送られたものか確認する
  - 正規の Provider から送られたものであれば Workload Identity Pool を確認して、Provider が提示した ID が借用可能な権限を確認し、一時的に権限を貸す(期限付きtokenを渡す)
- 言い換えるならば,GCP は OIDC のプロトコル上でGitHub Actions を信頼している. 次のように,あるレポジトリのGithub Actionsに権限を与えたとき、 Github Actionsから送られるレポジトリ情報は改竄されていないとみなしているということ.(この処理はレポジトリ`foo/bar`で実行されている、という情報がGitHub Actionsから送られたときに実際はレポジトリ`hoge/fuga`から送られているというケースが存在しない, というイメージ.)
-  攻撃者から見れば GCP のリソースにアクセスするには GitHub に成りすまさなければならない.
-  もちろん、Impersonate と Workload Identity Pool を利用したからと言って絶対に安心なわけではない. Impersonate するかしないかによらず、開発者の GitHub のアカウントがのっとられたら 当然強い権限でGCPなどの重要なリソースを操作できてしまう.
  ```
  gcloud iam service-accounts add-iam-policy-binding "${SERVICE_ACCOUNT}" \
  --project="${PROJECT_ID}" \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/${WORKLOAD_IDENTITY_POOL_ID}/attribute.repository/${GH_USER}/${GH_REPO}"
  ```

## GCP で Workload Identity Pool をセットアップする
terraform 用のサービスアカウントの作成

```sh
export PROJECT_ID=...
```

```sh
export TF_SA=...
```

```sh
gcloud iam service-accounts create $TF_SA --display-name "ci/cd terraform executor"
```

```sh
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member=serviceAccount:terraform-admin@$PROJECT_ID.iam.gserviceaccount.com \\
  --role=roles/editor
```

```sh
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member=serviceAccount:terraform-admin@$PROJECT_ID.iam.gserviceaccount.com \\
  --role=roles/cloudfunctions.admin
```

### Pool の作成
```sh
export GCP_WORKLOAD_ID_POOL=...
```
```sh
gcloud iam workload-identity-pools create $GCP_WORKLOAD_ID_POOL \
  --project="${PROJECT_ID}" \
  --location="global"
```

```sh
WORKLOAD_IDENTITY_POOL_ID=$(gcloud iam workload-identity-pools describe $GCP_WORKLOAD_ID_POOL \
  --project="${PROJECT_ID}" \
  --location="global" \
  --format="value(name)")
```

### Provider の設定

```sh
export OIDC_NAME=
```

GitHub Actions の例:

```sh
gcloud iam workload-identity-pools providers create-oidc $OIDC_NAME \
  --project="${PROJECT_ID}" \
  --location="global" \
  --workload-identity-pool=$GCP_WORKLOAD_ID_POOL \
  --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository" \
  --issuer-uri="https://token.actions.githubusercontent.com"
```

Provider の管理するIDが service account の権限を借用できるようにする.

```sh
export GH_USER=
```


```sh
export GH_REPO=
```

```sh
export TF_SERVICE_ACCOUNT=name@project.iam.gserviceaccount.com
```

```sh
gcloud iam service-accounts add-iam-policy-binding "${TF_SERVICE_ACCOUNT}" \
  --project="${PROJECT_ID}" \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/${WORKLOAD_IDENTITY_POOL_ID}/attribute.repository/${GH_USER}/${GH_REPO}"
```


Github Actions の workflow の設定

note: 


```yaml
jobs:
  ci:
    steps:
      - name: Authenticate to Google Cloud
        id: 'google-cloud-auth'
        uses: 'google-github-actions/auth@v0.4.1'
        with:
          create_credentials_file: true
          workload_identity_provider: "${{ secrets.GCP_WORKLOAD_ID_PROVIDER_ID }}" # $WORKLOAD_IDENTITY_POOL_ID/providers/$OIDC_NAME
          service_account: "${{ secrets.TF_GCP_SERVICE_ACCOUNT }}"
      - name: Google Cloud login
        run: gcloud auth login --brief --cred-file="${{ steps.google-cloud-auth.outputs.credentials_file_path }}"
```


