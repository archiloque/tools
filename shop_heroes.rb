#!/usr/bin/env ruby

require 'terminal-table'
require 'set'

SKILL_ARMOR_CRAFTING = "🛡"
SKILL_METAL_WORKING = "🤘"
SKILL_WEAPON_CRAFTING = "⚔"
SKILL_WOOD_WORKING = "🌳"
SKILL_ALCHEMY = "🜁"
SKILL_TEXTILE_WORKING = "👘"
SKILL_ARTS_AND_CRAFT = "🎨"

BASE_SKILLS = [
  SKILL_ARMOR_CRAFTING,
  SKILL_METAL_WORKING,
  SKILL_WEAPON_CRAFTING,
  SKILL_WOOD_WORKING,
  SKILL_ALCHEMY,
  SKILL_TEXTILE_WORKING,
  SKILL_ARTS_AND_CRAFT
]

SKILL_TINKERING = "🔧"
SKILL_MAGIC = "🎩"
SKILL_RUNE_WRITING = "ᚠ"
SKILL_JEWELRY = "💎"

ADVANCED_SKILLS = [
  SKILL_TINKERING,
  SKILL_MAGIC,
  SKILL_RUNE_WRITING,
  SKILL_JEWELRY
]

ALL_SKILLS = BASE_SKILLS + ADVANCED_SKILLS

WORKER_ARMORER = "armorer"
WORKER_BLACKSMITH = "blacksmith"
WORKER_CARPENTER = "carpenter"
WORKER_DRUID = "druid"
WORKER_LEATHER_WORKER = "leather worker"
WORKER_TAILOR = "tailor"

TIERS_1_WORKERS = [
  WORKER_ARMORER,
  WORKER_BLACKSMITH,
  WORKER_CARPENTER,
  WORKER_DRUID,
  WORKER_LEATHER_WORKER,
  WORKER_TAILOR
]

WORKER_FLETCHER = "fletcher"
WORKER_ARTIFICER = "artificer"
WORKER_ENCHANTER = "enchanter"
WORKER_JEWELER = "jeweler"
WORKER_LUTHIER = "luthier"
WORKER_SORCERESS = "sorceress"

TIERS_2_WORKERS = [
  WORKER_FLETCHER,
  WORKER_ARTIFICER,
  WORKER_ENCHANTER,
  WORKER_JEWELER,
  WORKER_LUTHIER,
  WORKER_SORCERESS,
]

WORKERS_SKILLS = {
  WORKER_ARMORER => [SKILL_ARMOR_CRAFTING, SKILL_METAL_WORKING],
  WORKER_BLACKSMITH => [SKILL_METAL_WORKING, SKILL_WEAPON_CRAFTING],
  WORKER_CARPENTER => [SKILL_WEAPON_CRAFTING, SKILL_WOOD_WORKING],
  WORKER_DRUID => [SKILL_ALCHEMY, SKILL_WOOD_WORKING],
  WORKER_LEATHER_WORKER => [SKILL_ARMOR_CRAFTING, SKILL_TEXTILE_WORKING],
  WORKER_TAILOR => [SKILL_ARTS_AND_CRAFT, SKILL_TEXTILE_WORKING],

  WORKER_ARTIFICER => [SKILL_ARMOR_CRAFTING, SKILL_TINKERING],
  WORKER_ENCHANTER => [SKILL_MAGIC, SKILL_RUNE_WRITING],
  WORKER_FLETCHER => [SKILL_ARTS_AND_CRAFT, SKILL_WEAPON_CRAFTING],
  WORKER_JEWELER => [SKILL_METAL_WORKING, SKILL_JEWELRY],
  WORKER_LUTHIER => [SKILL_TEXTILE_WORKING, SKILL_WOOD_WORKING],
  WORKER_SORCERESS => [SKILL_ALCHEMY, SKILL_MAGIC],
}


def create_table(number_of_workers, basic_workers, base_skills)
  target_skills = base_skills ? BASE_SKILLS : ALL_SKILLS
  table = Terminal::Table.new(:headings => ["#{number_of_workers}#{basic_workers ? ' tiers 1' : ''} workers"] + target_skills ) do |t|
    (basic_workers ? TIERS_1_WORKERS : (TIERS_1_WORKERS + TIERS_2_WORKERS)).combination(number_of_workers) do |combination|
      current_line = [combination.sort.join(', ')]
      workers_skills = Set.new
      combination.each do |worker|
        workers_skills = workers_skills | WORKERS_SKILLS[worker]
      end
      if base_skills
        workers_skills = workers_skills & BASE_SKILLS
      end
      current_line = ["#{(workers_skills.length == target_skills.length) ? "✓" : " "} #{combination.sort.join(', ')}"]
      target_skills.each do |s|
        current_line << ((workers_skills.include? s) ? "✓" : "")
      end
      t << current_line
    end
  end
  puts table
  STDOUT << "\n"
end

1.upto(6) do |number_of_workers|
  create_table(number_of_workers, true, true)
  create_table(number_of_workers, false, true)
  create_table(number_of_workers, false, false)
end
