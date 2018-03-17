require_relative 'color'
require_relative 'constants'
require_relative 'document'
require_relative 'model'
require_relative 'voxel'

# Render MagicaVoxel files, see https://github.com/ephtracy/voxel-model/blob/master/MagicaVoxel-file-format-vox.txt
class MagicaVoxel::Renderer

  include MagicaVoxel::Constants

  # @param [MagicaVoxel::Document] document
  def initialize(document)
    @document = document
  end

  # @return [String]
  def render
    @ascii_content = '' + FILE_HEADER
    add_4_byte_int(@ascii_content, 150)
    @ascii_content << MAIN_CHUNK_ID
    add_4_byte_int(@ascii_content, 0)

    @inner_content = ''
    @inner_content << PACK_CHUNK_ID
    add_4_byte_int(@inner_content, 4)
    add_4_byte_int(@inner_content, 0)
    add_4_byte_int(@inner_content, @document.models.length)

    @document.models.each do |model|
      @inner_content << MODEL_SIZE_CHUNK_ID
      add_4_byte_int(@inner_content, 12)
      add_4_byte_int(@inner_content, 0)
      add_4_byte_int(@inner_content, model.x)
      add_4_byte_int(@inner_content, model.y)
      add_4_byte_int(@inner_content, model.z)

      @inner_content << MODEL_VOXELS_CHUNK_ID
      add_4_byte_int(@inner_content, (model.voxels.length * 4) + 4)
      add_4_byte_int(@inner_content, 0)
      add_4_byte_int(@inner_content, model.voxels.length)
      model.voxels.each do |voxel|
        add_1_byte_int(@inner_content, voxel.x)
        add_1_byte_int(@inner_content, voxel.y)
        add_1_byte_int(@inner_content, voxel.z)
        add_1_byte_int(@inner_content, voxel.color_index)
      end
    end

    if @document.palette
      @inner_content << PALETTE_CHUNK_ID
      add_4_byte_int(@inner_content, 1024)
      add_4_byte_int(@inner_content, 0)
      0.upto(255) do |index_color|
        color = @document.palette[index_color]
        add_1_byte_int(@inner_content, color.r)
        add_1_byte_int(@inner_content, color.g)
        add_1_byte_int(@inner_content, color.b)
        add_1_byte_int(@inner_content, color.a)
      end

      0.upto(255) do |index_color|
        color = @document.palette[index_color]
        if (color.material != :diffuse) ||
            (color.weight != 1) ||
            (!color.attributes.empty?)
          @inner_content << MATERIAL_CHUNK_ID
          attributes_with_value = color.attributes.length - (color.attributes.key?(:istotalpower) ? 1 : 0)
          add_4_byte_int(@inner_content, (4 + attributes_with_value) * 4)
          add_4_byte_int(@inner_content, 0)
          add_4_byte_int(@inner_content, index_color + 1)
          add_4_byte_int(@inner_content, MagicaVoxel::Color::MATERIALS_NAME_TO_INDEX[color.material])
          add_4_byte_float(@inner_content, color.weight)
          properties = '0' * color.attributes.length
          MagicaVoxel::Color::ATTRIBUTES.each_with_index do |attribute_name, index|
            if color.attributes.key?(attribute_name)
              properties[index] = '1'
            end
          end
          add_4_byte_int(@inner_content, properties.to_i(2))
          MagicaVoxel::Color::ATTRIBUTES.each do |attribute_name|
            if color.attributes.key?(attribute_name) && (attribute_name != :istotalpower)
              add_4_byte_float(@inner_content, color.attributes[attribute_name])
            end
          end
        end
      end

    end

    add_4_byte_int(@ascii_content, @inner_content.length)
    @ascii_content << @inner_content
    @ascii_content
  end

  private

  # @param [Integer] content
  # @param [String] string
  # @return [nil]
  def add_4_byte_int(string, content)
    string << [content].pack('L*')
  end

  # @param [Float] content
  # @param [String] string
  # @return [nil]
  def add_4_byte_float(string, content)
    string << [content].pack('e*')
  end

  # @param [Integer] content
  # @param [String] string
  # @return [nil]
  def add_1_byte_int(string, content)
    string << [content].pack('C*')
  end

end