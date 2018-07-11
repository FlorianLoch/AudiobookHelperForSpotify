VERSION=0.4.0

.PHONY: install_spotify_sdk

install_spotify_sdk: ./app/libs/ ./app/libs/spotify-app-remote-release-$(VERSION).aar

./app/libs:
	mkdir ./app/libs

./app/libs/spotify-app-remote-release-$(VERSION).aar:
	curl -L https://github.com/spotify/android-app-remote-sdk/releases/download/v$(VERSION)/spotify-app-remote-release-$(VERSION).aar > ./app/libs/spotify-app-remote-release-$(VERSION).aar
