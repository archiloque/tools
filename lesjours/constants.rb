DATA_DIR = 'data'

OBSESSIONS_URI = 'https://lesjours.fr/obsessions/'
OBSESSIONS_HTML_FILE = File.join(DATA_DIR, 'obsessions.html')
OBSESSIONS_JSON_FILE = File.join(DATA_DIR, 'obsessions.json')
OBSESSIONS_WITH_EPISODES_JSON_FILE = File.join(DATA_DIR, 'obsessions_with_episodes.json')

OBSESSIONS_DIR = File.join(DATA_DIR, 'obsessions')
BOOK_DIR = File.join(DATA_DIR, 'book')

# The value of your session cookie
COOKIE_VALUE = 'XXX'

# Transformation script
IMAGE_TRANSFORMATION_SCRIPT = "convert -resize 360 -type grayscale"