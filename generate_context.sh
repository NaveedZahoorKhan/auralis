#!/bin/bash
mkdir -p .context
CONTEXT_FILE=".context/project_context.md"

echo "# Project Context" > "$CONTEXT_FILE"
echo "## Project Structure" >> "$CONTEXT_FILE"
tree -a -I "build|.gradle|.git|.context|*.jar" >> "$CONTEXT_FILE"

echo -e "\n## Files" >> "$CONTEXT_FILE"
find . -type f -not -path "*/build/*" -not -path "*/\.gradle/*" -not -path "*/\.git/*" -not -path "*/\.context/*" -not -path "*/gradle/wrapper/*" -not -name "gradlew*" -not -name "*.jar" -not -name "*.png" -not -name "*.webp" -not -name "*.jks" -not -name "*.so" -not -name "*.keystore" -not -name "*.sqlite" -not -name "generate_context.sh" | while read -r file; do
    echo -e "\n### $file\n" >> "$CONTEXT_FILE"
    echo '```' >> "$CONTEXT_FILE"
    cat "$file" >> "$CONTEXT_FILE"
    echo '```' >> "$CONTEXT_FILE"
done
