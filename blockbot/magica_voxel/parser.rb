require_relative 'color'
require_relative 'constants'
require_relative 'document'
require_relative 'model'
require_relative 'voxel'

# Parse MagicaVoxel files, see https://github.com/ephtracy/voxel-model/blob/master/MagicaVoxel-file-format-vox.txt
class MagicaVoxel::Parser

  include MagicaVoxel::Constants

  # @param [String] ascii_content
  def initialize(ascii_content)
    @ascii_content = ascii_content
  end

  # @return [MagicaVoxel::Document]
  def parse
    @current_position = 0
    header = fetch_4_bytes_string
    unless header == FILE_HEADER
      raise "Invalid file, header is [#{header}] instead of [#{FILE_HEADER}]"
    end
    check_4_bytes_value(150)
    main_chunk = fetch_4_bytes_string
    unless main_chunk == MAIN_CHUNK_ID
      raise "Invalid file, main chunk is [#{header}] instead of [#{MAIN_CHUNK_ID}]"
    end
    check_4_bytes_value(0)
    @document = MagicaVoxel::Document.new
    @declared_number_of_models = 1
    total_size = fetch_4_bytes_int
    while @current_position < (total_size + 20)
      read_next_chunk
    end
    unless @current_position == (total_size + 20)
      raise "Invalid file, content length is #{@current_position} but should be #{total_size}"
    end
    unless @document.models.length == @declared_number_of_models
      raise "Invalid file: #{@document.models.length} models found but #{@declared_number_of_models} declared"
    end
    @document
  end

  private

  # @return [nil]
  def read_next_chunk
    chunk_type = fetch_4_bytes_string
    case chunk_type
      when PACK_CHUNK_ID
        read_chunk_pack
      when MODEL_SIZE_CHUNK_ID
        read_chunk_size
      when MODEL_VOXELS_CHUNK_ID
        read_chunk_xyzi
      when PALETTE_CHUNK_ID
        read_chunk_rgba
      when MATERIAL_CHUNK_ID
        read_chunk_matt
      else
        raise "Unknown type [#{chunk_type}] at position #{@current_position - 3}"
    end
  end

  # @return [nil]
  def read_chunk_pack
    p 'Read pack'
    check_4_bytes_value(4)
    check_4_bytes_value(0)
    @declared_number_of_models = fetch_4_bytes_int
  end

  # @return [nil]
  def read_chunk_size
    p 'Read size'
    check_4_bytes_value(12)
    check_4_bytes_value(0)
    @document.models << MagicaVoxel::Model.new(
        fetch_4_bytes_int,
        fetch_4_bytes_int,
        fetch_4_bytes_int,
        )
  end

  # @return [nil]
  def read_chunk_xyzi
    p 'Read voxels'
    chunk_size = fetch_4_bytes_int
    check_4_bytes_value(0)
    num_voxels = fetch_4_bytes_int
    unless (chunk_size / 4) == (num_voxels + 1)
      raise "Invalid file : #{num_voxels} voxels doesn't match with declared length #{chunk_size}"
    end
    current_model = @document.models.last
    num_voxels.times do
      current_model.voxels << MagicaVoxel::Voxel.new(
          fetch_1_byte_int,
          fetch_1_byte_int,
          fetch_1_byte_int,
          fetch_1_byte_int,
          )
    end
  end

  # @return [nil]
  def read_chunk_rgba
    p 'Read palette'
    @document.palette = []
    check_4_bytes_value(1024)
    check_4_bytes_value(0)
    256.times do
      @document.palette << MagicaVoxel::Color.new(
          fetch_1_byte_int,
          fetch_1_byte_int,
          fetch_1_byte_int,
          fetch_1_byte_int,
          )
    end
  end

  # @return [nil]
  def read_chunk_matt
    p 'Read matt'
    fetch_4_bytes_int
    check_4_bytes_value(0)
    color_id = fetch_4_bytes_int
    color = @document.palette[color_id - 1]
    material = fetch_4_bytes_int
    color.material = MagicaVoxel::Color::MATERIALS_INDEX_TO_NAME[material]
    color.weight = fetch_4_bytes_float
    properties = fetch_4_bytes_int.to_s(2)
    MagicaVoxel::Color::ATTRIBUTES.each_with_index do |property_name, index|
      if properties[-(index + 1)] == '1'
        if property_name == :istotalpower
          color.attributes[:istotalpower] = 1
        else
          property_value = fetch_4_bytes_float
          color.attributes[property_name] = property_value
        end
      end
    end
  end

  # @return [String]
  def fetch_4_bytes_string
    value = @ascii_content[@current_position..(@current_position + 3)]
    @current_position += 4
    value
  end

  # @return [Float]
  def fetch_4_bytes_float
    value = @ascii_content[@current_position..(@current_position + 3)].unpack('e*').first
    @current_position += 4
    value
  end

  # @return [Integer]
  def fetch_4_bytes_int
    value = @ascii_content[@current_position..(@current_position + 3)].unpack('L*').first
    @current_position += 4
    value
  end

  # @return [Integer]
  def fetch_1_byte_int
    value = @ascii_content[@current_position].unpack('C*').first
    @current_position += 1
    value
  end

  # @param [Integer] value
  # @return [nil]
  def check_4_bytes_value(value)
    got_value = fetch_4_bytes_int
    unless got_value == value
      raise "Invalid file, at #{position - 4} expected #{value} and got #{got_value}"
    end
  end

end