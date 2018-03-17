require 'json'

require_relative 'magica_voxel'

class MagicaVoxel::Document

  attr_accessor :models
  attr_accessor :palette

  def initialize
    @models = []
  end

  def as_json(*)
    {
        models: models.collect{|m| m.as_json},
        palette: palette ? palette.collect{|c| c.as_json} : nil,
    }
  end

  def to_json(*options)
    as_json(*options).to_json(*options)
  end

end