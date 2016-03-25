/*
Copyright 2014 Google Inc. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package dan200.qcraft.shared;

import net.minecraftforge.fml.common.IWorldGenerator;
import dan200.qcraft.shared.blocks.QBlocks;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import java.util.Random;
import net.minecraft.util.BlockPos;

public class QuantumOreGenerator implements IWorldGenerator {
    
    private final int veinSize = 5;
    private final int spawnCyclesPerChunk = 4;
    private final int minGenY = 0;
    private final int maxGenY = 24;
    private WorldGenMinable m_oreGen;

    public QuantumOreGenerator() {
        m_oreGen = new WorldGenMinable(QBlocks.quantumOre.getDefaultState(), veinSize);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (!world.provider.isSurfaceWorld() && world.getWorldType() != WorldType.FLAT) {
            generateSurface(world, random, chunkX, chunkZ, spawnCyclesPerChunk, minGenY, maxGenY);
        }
    }

    private void generateSurface(World world, Random rand, int chunkX, int chunkZ, int chancesToSpawn, int minHeight, int maxHeight) {
        if (minHeight < 0 || maxHeight > 256 || minHeight > maxHeight) {
            throw new IllegalArgumentException("Illegal Height Arguments for WorldGenerator");
        }

        for (int k = 0; k < chancesToSpawn; ++k) {
            int heightDiff = maxHeight - minHeight + 1;
            int firstBlockXCoord = chunkX * 16 + rand.nextInt(16);
            int firstBlockYCoord = minHeight + rand.nextInt(heightDiff);
            int firstBlockZCoord = chunkZ * 16 + rand.nextInt(16);
            m_oreGen.generate(world, rand, new BlockPos(firstBlockXCoord, firstBlockYCoord, firstBlockZCoord));
        }
    }
}
