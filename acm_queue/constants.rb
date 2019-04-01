DATA_DIR = 'data'
MAIN_URI = 'https://queue.acm.org/pastissues.cfm'
MAIN_HTML_FILE = File.join(DATA_DIR, 'main.html')
ISSUES_JSON_FILE = File.join(DATA_DIR, 'issues.json')
ISSUES_WITH_ARTICLES_JSON_FILE = File.join(DATA_DIR, 'issues_with_articles_.json')
ISSUES_DIR = File.join(DATA_DIR, 'issues')
BOOK_DIR = File.join(DATA_DIR, 'book')

IMAGE_TRANSFORMATION_SCRIPT = "convert -resize 360 -type grayscale"