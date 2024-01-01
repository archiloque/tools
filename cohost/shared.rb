require "fileutils"

DATA_DIR_NAME = "data"
PAGES_DIR_NAME = "pages"
JSON_DIR_NAME = "json"
JSON_FILE_NAME = "main.json"
ASCIIDOCTOR_DIR_NAME = "asciidoctor"

def create_if_not_exist(dir_path)
  FileUtils.mkdir_p(dir_path) unless File.exist?(dir_path)
  dir_path
end

def asciidoctor_directory(blog_name, tag)
  create_if_not_exist(File.join(DATA_DIR_NAME, blog_name, ASCIIDOCTOR_DIR_NAME, tag))
end

def data_directory(blog_name)
  create_if_not_exist(File.join(DATA_DIR_NAME, blog_name))
end

def json_directory(blog_name)
  create_if_not_exist(File.join(DATA_DIR_NAME, blog_name, JSON_DIR_NAME))
end

def json_file(blog_name)
  File.join(json_directory(blog_name), JSON_FILE_NAME)
end

def page_directory(blog_name)
  create_if_not_exist(File.join(DATA_DIR_NAME, blog_name, PAGES_DIR_NAME))
end

