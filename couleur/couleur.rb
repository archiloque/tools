require 'magic'
require 'erb'

values = identify_main_colors 'test.jpg', 3
STDOUT << ERB.new(File.new('003.erb').read).result(binding)