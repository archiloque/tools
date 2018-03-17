require 'json'

require_relative 'magica_voxel'

class MagicaVoxel::Color

  attr_accessor :r, :g, :b, :a, :material, :weight, :attributes

  MATERIALS_INDEX_TO_NAME = {
      0 => :diffuse,
      1 => :metal,
      2 => :glass,
      3 => :emissive,
  }

  MATERIALS_NAME_TO_INDEX = MATERIALS_INDEX_TO_NAME.invert

  ATTRIBUTES = [
      :plastic,
      :roughness,
      :specular,
      :ior,
      :attenuation,
      :power,
      :glow,
      :istotalpower,
  ]

  def initialize(r, g, b, a)
    @r = r
    @g = g
    @b = b
    @a = a
    @material = :diffuse
    @weight = 1
    @attributes = {}
  end

  def as_json(*)
    {
        r: r,
        g: g,
        b: b,
        a: a,
        material: material,
        weight: weight,
        attributes: attributes
    }
  end

  def to_json(*options)
    as_json(*options).to_json(*options)
  end

end