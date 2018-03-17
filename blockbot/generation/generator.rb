require_relative '../magica_voxel/color'
require_relative 'generation'
require_relative 'models'
require_relative 'world'

class Generation::Generator

  def initialize
    @world = Generation::World.new(100, 100)
    @random = Random.new(0)
  end

  # @return [Generation::World]
  def generate
    256.times do
      add_color
    end

    add_floor

    250.times do
      add_piece
    end

    @world
  end

  private

  # @return [nil]
  def add_color
    color = MagicaVoxel::Color.new(
        @random.rand(0..254),
        @random.rand(0..254),
        @random.rand(0..254),
        @random.rand(0..254)
    )
    material_type = MagicaVoxel::Color::MATERIALS_INDEX_TO_NAME[@random.rand(0...MagicaVoxel::Color::MATERIALS_INDEX_TO_NAME.length - 1)]
    case material_type
      when :diffuse
      when :metal
        color.material = material_type
        color.weight = @random.rand(1.0)
        color.attributes[:roughness] = @random.rand(1.0)
        color.attributes[:specular] = @random.rand(1.0)
        color.attributes[:plastic] = @random.rand(0..1)
      when :glass
        color.material = material_type
        color.weight = @random.rand(1.0)
        color.attributes[:roughness] = @random.rand(1.0)
        color.attributes[:ior] = @random.rand(1.0)
        color.attributes[:attenuation] = @random.rand(1.0)
    end

    @world.colors << color
  end

  # @return [nil]
  def add_floor
    0.upto(@world.x - 1) do |x|
      0.upto(@world.y - 1) do |y|
        @world.set_block(x, y, 0, 0)
      end
    end
  end

  # @return [nil]
  def add_piece

    # Create an array of probabilities to find the column :
    # the number of entries is proportional to the max z of a column
    probabilities = []
    0.upto((@world.x / 3) - 1) do |x3|
      x = x3 * 3
      0.upto((@world.y / 3) - 1) do |y3|
        y = y3 * 3
        max_z = @world.max_z(x, y)
        probabilities.concat(Array.new(max_z ^ 3, [x, y]))
      end
    end
    x, y = probabilities[@random.rand(probabilities.length)]

    model = Generation::Models::MODELS[@random.rand(Generation::Models::MODELS.length)]

    # Does it fit into the world ?
    if ((model.x + x) >= @world.x) || ((model.y + y) >= @world.y)
      return
    end

    column = @world.blocks[x][y]
    min_z = 0

    if @random.rand > 0.5
      # Find lower position where we can put it, if we can avoid putting it at the top
      column.keys.sort[0..-2].each do |z|
        if can_put_model(model, x, y, z)
          min_z = z + 1
        end
      end
    end

    # If not found, try to put it under another piece
    if min_z == 0
      if @random.rand > 0.5
        column.keys.sort.reverse.each do |z|
          possible_z = z - (model.z + 1)
          if possible_z > 0
            if can_put_model(model, x, y, possible_z)
              min_z = possible_z + 1
            end
          end
        end
      end
    end

    # if not found, put it on top
    if min_z == 0
      x.upto(x + model.x - 1) do |x2|
        y.upto(y + model.y - 1) do |y2|
          min_z = [min_z, @world.max_z(x2, y2)].max
        end
      end
    end

    color = @random.rand(0..254)

    add_model(x, y, min_z, model, color)
  end

  # @param [Integer] x
  # @param [Integer] y
  # @param [Integer] z
  # @param [Generation::Model] model
  # @param [Integer] color
  def add_model(x, y, z, model, color)
    x.upto(x + model.x - 1) do |x2|
      y.upto(y + model.y - 1) do |y2|
        z.upto(z + model.z - 1) do |z2|
          @world.set_block(x2, y2, z2, color)
        end
      end
    end
  end

  # @param [Generation::Model] model
  # @param [Integer] x
  # @param [Integer] y
  # @param [Integer] z
  # @return [TrueClass|FalseClass]
  def can_put_model(model, x, y, z)
    x.upto(x + model.x - 1) do |x2|
      y.upto(y + model.y - 1) do |y2|
        column = @world.blocks[x2][y2]
        (z + 1).upto(z + model.z) do |z2|
          if column.key?(z2)
            return false
          end
        end
      end
    end
    true
  end


end