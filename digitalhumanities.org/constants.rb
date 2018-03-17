DATA_DIR = 'data'

ISSUES_JSON_FILE = File.join(DATA_DIR, 'issues.json')
ARTICLES_JSON_FILE = File.join(DATA_DIR, 'articles.json')
FIGURES_JSON_FILE = File.join(DATA_DIR, 'figures.json')
ARTICLES_WITH_ID_JSON_FILE = File.join(DATA_DIR, 'articles_with_id.json')
ARTICLES_DIR = File.join(DATA_DIR, 'articles')
BOOK_DIR = File.join(DATA_DIR, 'book')
FIGURES_DIR = File.join(DATA_DIR, 'figures')
CONVERTED_FIGURES_DIR = File.join(DATA_DIR, 'converted')

# Transformation script
IMAGE_TRANSFORMATION_SCRIPT = "convert -resize 360 -type grayscale"