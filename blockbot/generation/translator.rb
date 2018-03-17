require_relative 'generation'
require_relative '../magica_voxel/color'
require_relative '../magica_voxel/document'
require_relative '../magica_voxel/model'
require_relative '../magica_voxel/voxel'

class Generation::Translator

  # @param [Generation::World] world
  # @return [MagicaVoxel::Document]
  def translate(world)
    document = MagicaVoxel::Document.new

    document.palette = world.colors

    model = MagicaVoxel::Model.new(
        world.x,
        world.y,
        world.z,
        )
    0.upto(world.x - 1) do |x|
      0.upto(world.y - 1) do |y|
        column = world.blocks[x][y]
        column.each_pair do |index, color|
          model.voxels << MagicaVoxel::Voxel.new(
              x,
              y,
              index,
              color + 1
          )
        end
      end
    end
    document.models << model
    document
  end


end