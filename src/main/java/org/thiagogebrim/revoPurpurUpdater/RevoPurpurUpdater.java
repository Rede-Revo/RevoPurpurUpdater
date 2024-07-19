package org.thiagogebrim.revoPurpurUpdater;

import org.bukkit.plugin.java.JavaPlugin;

public final class RevoPurpurUpdater extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("RevoPurpurUpdater está em modo espera, pronto para atualizar o Purpur no desligamento.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Iniciando a busca por atualizações...");

        // Atualiza o Purpur
        PurpurUpdater.updatePurpur(getLogger());

        // Atualiza o LuckPerms
        LuckPermsUpdater.updateLuckPerms(getLogger());

        // Atualiza o ProtocolLib
        ProtocolLibUpdater.updateProtocolLib(getLogger());
    }
}
