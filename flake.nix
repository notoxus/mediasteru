{
  description = "MediaSteru Development Environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs { inherit system; };
    in
    {
      devShells.${system}.default = pkgs.mkShell {
        buildInputs = with pkgs; [
          jdk21
          gradle
          mpv
          yt-dlp
          ffmpeg
          docker
          docker-compose
          nodejs
          electron
        ];

        shellHook = ''
          export ELECTRON_SKIP_BINARY_DOWNLOAD=1
          export ELECTRON_OVERRIDE_DIST_PATH="${pkgs.electron}/bin"
          # If u use niri
          export ELECTRON_OZONE_PLATFORM_HINT="auto"
          echo "OK"
        '';
      };
    };
}