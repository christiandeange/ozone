# Production Releases

1. Make sure the `CHANGELOG.md` is updated with all the latest notable changes.
2. Run the release script. Any unsaved local changes will be ignored.
   ```shell
   ./release.sh <next-snapshot-version>
   ```
3. Push the commits. A new release will automatically be published on Sonatype.
   ```shell
   git push && git push --tags
   ```
