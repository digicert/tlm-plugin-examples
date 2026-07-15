#!/bin/bash

# The TLM Plugin SDK is served from a public GitHub Pages Maven repository
# (https://digicert.github.io/tlm-plugins-sdk-dist), so no authentication is required.

./mvnw clean package -s settings.xml -U

if [ $? -ne 0 ]; then
    echo "Build failed. Exiting."
    exit 1
fi

pushd ./plugin-dist
CHECKSUM_FILE="checksums"
echo ""> $CHECKSUM_FILE
echo "SHA2 checksum for TPM" >> $CHECKSUM_FILE
echo "\`\`\`" >> $CHECKSUM_FILE
find . -name "*.zip" | xargs sha256sum | cut --complement -c66-68 >> $CHECKSUM_FILE
echo "\`\`\`" >> $CHECKSUM_FILE
cat $CHECKSUM_FILE
popd
