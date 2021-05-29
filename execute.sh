#!/bin/bash
set -e -x
# Grab cache versions from commits if not provided
[[ -z "${OLD_CACHE}" || -z "${NEW_CACHE}" ]] && curl -s https://api.github.com/repos/abextm/osrs-cache/commits > commits.json
[[ -z "${OLD_CACHE}" ]] && export OLD_CACHE=$(jq -r '.[1].commit.message' commits.json | sed 's/Cache version //')
[[ -z "${NEW_CACHE}" ]] && export NEW_CACHE=$(jq -r '.[0].commit.message' commits.json | sed 's/Cache version //')

# Set outputs to use the cache versions in next steps
echo "::set-output name=old_cache::${OLD_CACHE}"
echo "::set-output name=new_cache::${NEW_CACHE}"

# Download the caches from abextm/osrs-cache as tarball archives
for CACHE in $OLD_CACHE $NEW_CACHE; do
  wget -q https://api.github.com/repos/abextm/osrs-cache/tarball/refs/tags/$CACHE -O $CACHE.tar.gz;
  mkdir -p cache/$CACHE/sprites;
  tar -xzf $CACHE.tar.gz --strip 1 -C cache/$CACHE;
done

curl -o src/main/java/melky/SpriteOverride.java https://raw.githubusercontent.com/melkypie/resource-packs/master/src/main/java/melky/resourcepacks/SpriteOverride.java
sed -i 's/package melky\.resourcepacks/package melky/' src/main/java/melky/SpriteOverride.java
chmod +x gradlew
./gradlew clean shadowJar
# Dump the sprites using correct sizing
java -Dcache.folder="cache/" -Dcache.old=$OLD_CACHE -Dcache.new=$NEW_CACHE -jar shadow.jar dump

# Add the changed/modified/deleted/renamed sprites to their respective directories
# Also perform a diff in case of modified
# May throw an error if the images are different widths
cd cache && mkdir -p modified/diff && mkdir -p added && mkdir deleted && mkdir renamed
git --no-pager diff --diff-filter=ADRM --no-index --name-status $OLD_CACHE/sprites/ $NEW_CACHE/sprites/ \
| awk '{switch ($1) { \
    case "A": \
      system("cp "$2" added/"); \
      break; \
    case "D": \
      system("cp "$2" deleted/"); \
      break; \
    case /R[[:digit:]]+/: \
      img1 = gensub(/.*sprites\/(.*)\.png/, "\\1", "g", $2); \
      img2 = gensub(/.*sprites\/(.*)\.png/, "\\1", "g", $3); \
      system("cp "$3" renamed/"img1"=to="img2".png"); \
      break; \
    case "M": \
      img=$2; \
      gsub(ENVIRON["OLD_CACHE"]"/sprites/", "", img); \
      system("compare -compose src "$2" "ENVIRON["NEW_CACHE"]"/sprites/"img" modified/diff/"img" || true"); \
      system("convert "$2" modified/diff/"img" "ENVIRON["NEW_CACHE"]"/sprites/"img" +append modified/"img" || true"); \
      break; \
}}'

# Generate a spritesheet of the changes
declare -A diff=( ["deleted"]="#CC0000" ["added"]="DarkGreen" ["modified"]="DarkOrange3" )
for key in "${!diff[@]}"; do
  if [[ `ls -1 $key/*.png 2>/dev/null | wc -l` == 0 ]]; then
      unset diff[$key]
      continue;
  fi
  convert -background transparent -font Lato-Bold -pointsize 25 -fill ${diff[$key]} label:$key ${key}_text.png
  set -e +x
  convert -background transparent ${key}_text.png $key/*.png -append $key.png
  set -e -x
done
diffs=(${!diff[@]})
[[ ${#diffs[@]} > 0 ]] && convert -background transparent ${diffs[@]/%/.png} -background transparent -splice 10x0+0+0 +append -chop 10x0+0+0 +append $NEW_CACHE-spritesheet-diff.png

# Zip up the newest cache sprites and the diff folders
zip -qr $NEW_CACHE-sprites.zip $NEW_CACHE/sprites
zip -qr $OLD_CACHE--$NEW_CACHE-diff.zip added/ renamed/ modified/ deleted/