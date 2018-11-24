import sys
import shutil
import os

if __name__ == '__main__':
    if len(sys.argv) != 2:
        print "Usage: build_aix.py <API KEY>"
        exit(1)
    api_key = sys.argv[1]

    with open('com.kylecorry.ml4k/assets/api.txt', 'w+') as f:
        f.write(api_key)

    if os.path.exists('com.kylecorry.ml4k.aix'):
        os.remove('com.kylecorry.ml4k.aix')
    shutil.make_archive('com.kylecorry.ml4k', 'zip', base_dir = 'com.kylecorry.ml4k')
    os.rename('com.kylecorry.ml4k.zip', 'com.kylecorry.ml4k.aix')
