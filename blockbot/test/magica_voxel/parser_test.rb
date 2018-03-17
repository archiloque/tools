require 'json'

require_relative '../test_helper'
require_relative '../../magica_voxel/parser'
require_relative '../../magica_voxel/renderer'

class MagicaVoxel::ParserTest < Minitest::Test

  def test_chr_sword
    Dir.glob(File.join(__dir__, '*.vox')).each do |file_path|
      p "Parsing [#{file_path}]"
      initial_content = IO.binread(file_path)
      document = MagicaVoxel::Parser.new(
          initial_content
      ).parse
      p "Parsing over, rendering"
      rendered_content = MagicaVoxel::Renderer.new(document).render
      IO.write("#{file_path}.json", JSON.pretty_generate(document))
      IO.binwrite("#{file_path}.out", rendered_content)
      assert_equal(initial_content, rendered_content)
      p "Rendering over"
    end
  end

end