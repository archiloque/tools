require 'json'

require_relative 'magica_voxel'

class MagicaVoxel::Model

  attr_accessor :x, :y, :z, :voxels

  def initialize(x, y, z)
    @x = x
    @y = y
    @z = z
    @voxels = []
  end

  def as_json(*)
    {
        x: x,
        y: y,
        z: z,
        voxels: voxels.collect {|v| v.as_json},
    }
  end

  def to_json(*options)
    as_json(*options).to_json(*options)
  end

end