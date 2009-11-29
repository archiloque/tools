# Flatten a directory tree
# By adding the directory names to the files

require 'FileUtils'

PATH_SEPARATOR = '_'

if ARGV.length < 1
  raise "Missing directory argument"
end
unless File.directory? ARGV[0]
  raise "\"#{ARGV[0]}\" isn't a directory"
end

FileUtils.mkdir "#{ARGV[0]}_flat"

Dir.glob("#{ARGV[0]}/**/*.*") do |file|
  unless File.directory? file
    FileUtils.cp file, "#{ARGV[0]}_flat/#{file[(ARGV[0].size() + 1) .. file.size].gsub("/", PATH_SEPARATOR)}"
  end
end
