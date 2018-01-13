from subprocess import call
from random import randint
from random import random
import os
import pickle
import datetime


# Вероятности
p = 40        # Кусочное скрещивание
q = 50        # Переброс в поэлементном скрещивании
mutation = 4  # Мутация

# Ограничения эволюции
average_limit = 60  # Предел среднего значения фитнесс-функции
max_limit = 85      # Предел максимального значения фитнесс-функции

# Параметры генофонда
head_size = 50                               # Размер "головы" хромосомы
max_arity = 4                                # Максимальная арность генофункции
population_size = 101                        # Размер популяции
chromosome_size = head_size * max_arity + 1  # Число генов в хромосоме
chromosome_count = 3                         # Число хромосом
terminal_count = 31                          # Число терминальных символов
nonterminal_count = 21                       # Число нетерминальных символов

# Параметры обучающего боя
training_robot = 'falcevor.Falcevor*'  # Имя обучаемого робота
enemy_robots = ['sample.Corners']        # Список противников
battlefield_width = 800                # Ширина поля
battlefield_height = 600               # Высота поля
round_count = 20                       # Число раундов
gun_cooling_rate = 0.1                 # Коэффициент охлаждения орудий
time_out = 450                         # Максимальное время бездействия


class Chromosome:
    def __init__(self, genes=None):
        self.fitness = 0

        if genes and len(genes) == chromosome_size:
            self.genes = genes
        else:
            self.genes = self.generate_genes()

    def __str__(self):
        return ' '.join(str(gene) for gene in self.genes)

    def generate_genes(self):
        genes = []
        for i in range(0, head_size):
            genes.append(randint(0, nonterminal_count + terminal_count - 1))

        for i in range(0, chromosome_size-head_size):
            genes.append(randint(nonterminal_count, nonterminal_count + terminal_count - 1))

        return genes


class Population:
    def __init__(self, body_angle=None, gun_angle=None, move_distance=None):
        self.body_angle = None
        self.gun_angle = None
        self.move_distance = None
        self.top_chromosomes = []
        self.avg_gun_fitness = 0
        self.avg_move_fitness = 0
        self.max_gun_fitness = 0
        self.max_move_fitness = 0
        self.sum_gun_fitness = 0
        self.sum_move_fitness = 0

        if body_angle and len(body_angle) == population_size:
            self.body_angle = body_angle
        else:
            self.body_angle = self.generate_chromosomes()

        if gun_angle and len(gun_angle) == population_size:
            self.gun_angle = gun_angle
        else:
            self.gun_angle = self.generate_chromosomes()

        if move_distance and len(move_distance) == population_size:
            self.move_distance = move_distance
        else:
            self.move_distance = self.generate_chromosomes()

    def generate_chromosomes(self):
        chromosomes = []
        for i in range(0, population_size):
            chromosomes.append(Chromosome())
        return chromosomes

    def upload(self, file):
        with open(file, 'wb') as output:
            pickle.dump(self, output, pickle.HIGHEST_PROTOCOL)

    def upload_person(self, file, num):
        if num < 0 or num >= population_size:
            return

        with open(file, 'w') as output:
            output.write(str(self.body_angle[num]) + '\n')
            output.write(str(self.gun_angle[num]) + '\n')
            output.write(str(self.move_distance[num]))


def create_battle():
    battle_file = open(r'C:\robocode\battles\train.battle', 'w', encoding='utf-8')
    template = r'''
        robocode.battleField.width={0}
        robocode.battleField.height={1}
        robocode.battle.numRounds={2}
        robocode.battle.gunCoolingRate={3}
        robocode.battle.rules.inactivityTime={4}
        robocode.battle.selectedRobots={5}
        '''

    body = template.format(
        battlefield_width,
        battlefield_height,
        round_count,
        gun_cooling_rate,
        time_out,
        training_robot + ',' + ','.join(enemy_robots)
    )

    battle_file.write(body)
    battle_file.close()


def run_battle():
    os.chdir('C:/robocode')

    call(['java', '-Xmx512M', '-Dsun.io.useCanonCaches=false', '-cp', r'C:\robocode\libs\robocode.jar',
          'robocode.Robocode', '-battle', r'C:\robocode\battles\train.battle', '-nodisplay', '-results',
          r'C:\robocode\genetic_robocode\results.txt'])


def fitness(population, num):
    result = open(r'C:\robocode\robots\falcevor\Falcevor.data\result.txt')
    params = result.read().split(' ')
    shots = float(params[0])
    hits = float(params[1])
    enemy_shots = float(params[2])
    enemy_hits = float(params[3])
    wall_collides = int(params[4])
    enemy_collides = int(params[5])

    population.gun_angle[num].fitness = (hits / shots) * 100

    move_fitness = ((enemy_shots - enemy_hits) / enemy_shots) * 100
    for i in range(0, wall_collides + enemy_collides):
        move_fitness *= 0.95
    population.body_angle[num].fitness = move_fitness
    population.move_distance[num].fitness = move_fitness


def rank_population(pop: Population):
    pop.body_angle = sorted(pop.body_angle, key=lambda x: x.fitness)[::-1]
    pop.gun_angle = sorted(pop.gun_angle, key=lambda x: x.fitness)[::-1]
    pop.move_distance = sorted(pop.move_distance, key=lambda x: x.fitness)[::-1]

    pop.max_move_fitness = pop.body_angle[0].fitness
    pop.max_gun_fitness = pop.gun_angle[0].fitness
    pop.sum_move_fitness = sum(value.fitness for value in pop.body_angle)
    pop.sum_gun_fitness = sum(value.fitness for value in pop.gun_angle)
    pop.avg_move_fitness = pop.sum_move_fitness / population_size
    pop.avg_gun_fitness = pop.sum_gun_fitness / population_size

    top_chromosomes = []
    top_chromosomes.extend(get_top_chromosomes(pop.body_angle))
    top_chromosomes.extend(get_top_chromosomes(pop.gun_angle))
    top_chromosomes.extend(get_top_chromosomes(pop.move_distance))
    pop.top_chromosomes = top_chromosomes


def get_top_chromosomes(chroms):
    top_chromosomes = []
    current = chroms[0]
    top_chromosomes.append(current)
    i = 1
    while chroms[i].fitness >= current.fitness * 0.97:
        current = chroms[i]
        top_chromosomes.append(current)
        i += 1
    return top_chromosomes


def estimate_population(pop: Population):
    create_battle()
    for person in range(0, population_size):
        pop.upload_person(r'C:\robocode\robots\falcevor\Falcevor.data\chromosome.dat', person)
        run_battle()
        fitness(pop, person)
    rank_population(pop)


def crossover_population(pop: Population):
    crossover_line(pop.gun_angle, create_pairs(pop.gun_angle, pop.sum_gun_fitness, pop))
    crossover_line(pop.body_angle, create_pairs(pop.body_angle, pop.sum_move_fitness, pop))
    crossover_line(pop.move_distance, create_pairs(pop.move_distance, pop.sum_move_fitness, pop))


def crossover_line(uno, duo):
    for i in range(0, population_size):
        if duo[i] is None:
            continue

        if randint(0, 99) < p:
            first = randint(0, chromosome_size-2)
            second = randint(first, chromosome_size-1)
            for j in range(first, second):
                tmp = uno[i].genes[j]
                uno[i].genes[j] = duo[i].genes[j]
                duo[i].genes[j] = tmp
            # for j in range(0, first):
            #     tmp = uno[i].genes[j]
            #     uno[i].genes[j] = duo[i].genes[j]
            #     duo[i].genes[j] = tmp
            # for j in range(second, chromosome_size):
            #     tmp = uno[i].genes[j]
            #     uno[i].genes[j] = duo[i].genes[j]
            #     duo[i].genes[j] = tmp
        else:
            for j in range(0, chromosome_size):
                if randint(0, 99) < q:
                    tmp = uno[i].genes[j]
                    uno[i].genes[j] = duo[i].genes[j]
                    duo[i].genes[j] = tmp


def create_pairs(chroms, fitness_sum, pop: Population):
    pairs = []
    for chrom in chroms:
        if chrom in pop.top_chromosomes:
            pairs.append(None)
        else:
            value = fitness_sum * random()
            sum = chroms[0].fitness
            i = 0
            while sum < value:
                i += 1
                sum += chroms[i].fitness
            pairs.append(Chromosome(chroms[i].genes[:]))
    return pairs


def mutate_population(pop: Population):
    chromosomes = []
    chromosomes.extend(pop.body_angle)
    chromosomes.extend(pop.gun_angle)
    chromosomes.extend(pop.move_distance)

    for chrom in chromosomes:
        if chrom in pop.top_chromosomes:
            continue
        for i in range(0, chromosome_size):
            if randint(0, 99) < mutation:
                chrom.genes[i] = randint(0, nonterminal_count + terminal_count - 1) if i < head_size \
                    else randint(nonterminal_count, nonterminal_count + terminal_count - 1)


def load_population(file):
    with open(file, 'rb') as input:
        return pickle.load(input)


def main(file=None):
    if file:
        pop = load_population(file)
    else:
        pop = Population()

    pop_folder = 'C:\\robocode\\genetic_robocode\\' + datetime.datetime.now().strftime("%Y-%m-%d %H.%M.%S") + '\\'
    os.makedirs(pop_folder)

    generation = 0
    estimate_population(pop)
    pop.upload(pop_folder + str(generation) + '.gen')

    log = open(r'C:\robocode\genetic_robocode\log.txt', 'a', encoding='utf-8')
    log.write('#{0}\n'.format(str(generation)))
    log.write('avg_move_fitness = {0:.3f};'.format(pop.avg_move_fitness))
    log.write('max_move_fitness = {0:.3f};\n'.format(pop.max_move_fitness))
    log.write('avg_gun_fitness = {0:.4f};'.format(pop.avg_gun_fitness))
    log.write('max_gun_fitness = {0:.3f};\n'.format(pop.max_gun_fitness))
    log.write('______________________________________________\n')
    log.close()

    while pop.avg_move_fitness < average_limit or pop.avg_gun_fitness < average_limit \
            or pop.max_gun_fitness < max_limit or pop.max_move_fitness < max_limit:
        crossover_population(pop)
        mutate_population(pop)
        estimate_population(pop)
        pop.upload_person('best.dat', 0)

        generation += 1
        pop.upload(pop_folder + str(generation) + '.gen')

        log = open(r'C:\robocode\genetic_robocode\log.txt', 'a', encoding='utf-8')
        log.write('#{0}\n'.format(str(generation)))
        log.write('avg_move_fitness = {0:.3f};'.format(pop.avg_move_fitness))
        log.write('max_move_fitness = {0:.3f};\n'.format(pop.max_move_fitness))
        log.write('avg_gun_fitness = {0:.4f};'.format(pop.avg_gun_fitness))
        log.write('max_gun_fitness = {0:.3f};\n'.format(pop.max_gun_fitness))
        log.write('______________________________________________\n')
        log.close()


main()