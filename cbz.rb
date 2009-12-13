# Get images cbz and zip files in a directory recursively
# And put them in cbz files with a fixed number of images per files

require 'rubygems'
require 'zip/zip'
require 'FileUtils'

NUMBER_OF_FILES_PER_ARCHIVE = 25

if ARGV.length < 1
  raise "Missing directory argument"
end
unless File.directory? ARGV[0]
  raise "\"#{ARGV[0]}\" isn't a directory"
end

BASENAME = File.basename ARGV[0]

TARGET_DIRECTORY = "#{BASENAME}_cbr"

if File.exist? TARGET_DIRECTORY
  raise "Target directory [#{TARGET_DIRECTORY}] already exists"
end
FileUtils.mkdir TARGET_DIRECTORY

current_archive_index = 0
file_index_in_current_file = 0

def create_archive index
  Zip::ZipFile.open("#{TARGET_DIRECTORY}/#{BASENAME}#{sprintf("%03d", index)}.cbz", Zip::ZipFile::CREATE)
end

def create_curret_file_name current_file, current_file_index
  "#{sprintf("%0#{NUMBER_OF_FILES_PER_ARCHIVE.to_s.length}d", current_file_index)}#{File.extname(current_file)}"
end

current_archive = create_archive current_archive_index

def image? file_name, full_file_name = file_name
  if ['.gif', '.jpg', '.png', '.jpeg'].include? File.extname(file_name)
    # p "Adding image file [#{full_file_name}]"
    true
  else
    unless "" == File.extname(file_name)
      p "Ignoring non image file [#{full_file_name}]"
    end
    false
  end
end

Dir.glob("#{ARGV[0]}/**/*.*").sort.each do |source_file|
  if ['.zip', '.cbz'].include? File.extname(source_file)
    files_to_add = []

    Zip::ZipFile.foreach source_file do |current_file|
      if image? current_file.name, "#{zip_source_file}@#{current_file.name}"
        files_to_add << current_file.name
      end
    end

    Zip::ZipFile.open source_file do |opened_zip_file|
      files_to_add.sort.each do |current_file|

        current_entry = opened_zip_file.get_entry current_file
        current_archive.get_output_stream(create_curret_file_name(current_file, file_index_in_current_file)) do |output_stream|
          current_entry.get_input_stream do |input_stream|
            IOExtras.copy_stream output_stream, input_stream
          end
        end

        if file_index_in_current_file == (NUMBER_OF_FILES_PER_ARCHIVE - 1)
          current_archive.close
          current_archive_index += 1
          current_archive = create_archive current_archive_index
          file_index_in_current_file = 0
        else
          file_index_in_current_file += 1
        end
      end
    end
  elsif image? source_file

    current_archive.get_output_stream(create_curret_file_name(source_file, file_index_in_current_file)) do |output_stream|
      File.open(source_file, "r") do |opened_file|
        IOExtras.copy_stream output_stream, opened_file
      end
    end

    if file_index_in_current_file == (NUMBER_OF_FILES_PER_ARCHIVE - 1)
      current_archive.close
      current_archive_index += 1
      current_archive = create_archive current_archive_index
      file_index_in_current_file = 0
    else
      file_index_in_current_file += 1
    end

  end
end
current_archive.close