require 'json'

require_relative 'magica_voxel'

class MagicaVoxel::Voxel

  attr_accessor :x, :y, :z, :color_index

  def initialize(x, y, z, color_index)
    @x = x
    @y = y
    @z = z
    @color_index = color_index
  end

  def as_json(*)
    {
        x: x,
        y: y,
        z: z,
        color_index: color_index,
    }
  end

  def to_json(*options)
    as_json(*options).to_json(*options)
  end

end