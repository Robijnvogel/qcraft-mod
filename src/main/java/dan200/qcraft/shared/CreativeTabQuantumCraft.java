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

import dan200.QCraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CreativeTabQuantumCraft extends CreativeTabs {

    public CreativeTabQuantumCraft(int p1, String p2) {
        super(p1, p2);
    }

    @Override
    public Item getTabIconItem() {
        return QCraft.Items.quantumDust;
    }

    @Override
    public String getTranslatedTabLabel() {
        return getTabLabel();
    }
}
