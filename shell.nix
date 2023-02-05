let
  pkgs = import <nixpkgs> { };
in pkgs.mkShell {
  packages = [
    pkgs.terraform
    pkgs.tflint
    pkgs.google-cloud-sdk
    pkgs.git
    pkgs.graphviz
  ];
}
