#!/usr/bin/env sh
#cp app/build/outputs/apk/release/app-release.apk DL2-$TRAVIS_BUILD_NUMBER.apk
curl -F chat_id="-1001289629950" -F text="$(./scripts/changelog.sh)" -F parse_mode="HTML" https://api.telegram.org/bot$BOT_TOKEN/sendMessage

echo $(./scripts/changelog.sh)
