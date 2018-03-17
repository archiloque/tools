require_relative 'generation'

class Generation::World

  attr_reader :x, :y, :colors, :blocks

  def initialize(x, y)
    @x = x
    @y = y
    @blocks = Array.new(x) {Array.new(y) {{}}}
    @colors = []
  end

  # @param [Integer] x
  # @param [Integer] y
  # @param [Integer] z
  # @param [Integer\nil] block
  def set_block(x, y, z, block)
    column = @blocks[x][y]
    if block.nil?
      column.remove(z)
    else
      column[z] = block
    end
  end

  # @param [Integer] x
  # @param [Integer] y
  # @param [Integer] z
  def get_block(x, y, z)
    @blocks[x][y][z]
  end

  # @param [Integer] x
  # @param [Integer] y
  def max_z(x, y)
    column = @blocks[x][y]
    if column.empty?
      0
    else
      column.keys.sort.last + 1
    end
  end

  def z
    max_z = -1
    0.upto(@x - 1) do |x|
      0.upto(@x - 1) do |y|
        column = @blocks[x][y]
        unless column.empty?
          max_z = [max_z, column.keys.sort.last].max
        end
      end
    end
    max_z
  end

end