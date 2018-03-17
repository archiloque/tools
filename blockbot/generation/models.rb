require_relative 'generation'
require_relative 'model'

module Generation::Models

  # 2x2
  MODEL_3437 = Generation::Model.new(6, 6, 4)

  # 2x4
  MODEL_3011_1 = Generation::Model.new(6, 12, 4)
  MODEL_3011_2 = Generation::Model.new(12, 6, 4)

  # 2x4 flat
  MODEL_40666_1 = Generation::Model.new(6, 12, 2)
  MODEL_40666_2 = Generation::Model.new(12, 6, 2)

  # 1x2x2
  MODEL_76371_1 = Generation::Model.new(6, 3, 8)
  MODEL_76371_2 = Generation::Model.new(3, 6, 8)

  # 4X4 flat
  MODEL_14721 = Generation::Model.new(12, 12, 2)

  MODELS = [
      MODEL_3437,
      MODEL_3011_1,
      MODEL_3011_2,
      MODEL_40666_1,
      MODEL_40666_2,
      MODEL_76371_1,
      MODEL_76371_2,
      MODEL_14721,
  ]

end