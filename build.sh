#!/usr/bin/env bash

mvn clean package
rm target/utt
echo "#!/usr/bin/env -S java -jar" >> target/utt
cat target/utt-*.jar >> target/utt
chmod +x target/utt
echo ">> Done!"