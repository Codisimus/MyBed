
package MyBed;

import org.bukkit.event.server.ServerListener;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijikokun.register.payment.Methods;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

/**
 * Checks for plugins whenever one is enabled
 *
 */
public class PluginListener extends ServerListener {
    public PluginListener() { }
    private Methods methods = new Methods();
    protected static Boolean useOP;

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if (MyBed.permissions == null && !useOP) {
            Plugin permissions = MyBed.pm.getPlugin("Permissions");
            if (permissions != null) {
                MyBed.permissions = ((Permissions)permissions).getHandler();
                System.out.println("[MyBed] Successfully linked with Permissions!");
            }
        }
        if (Register.economy == null)
            System.err.println("[MyBed] Config file outdated, Please regenerate");
        else if (!methods.hasMethod()) {
            try {
                methods.setMethod(MyBed.pm.getPlugin(Register.economy));
                if (methods.hasMethod()) {
                    Register.econ = methods.getMethod();
                    System.out.println("[MyBed] Successfully linked with "+
                            Register.econ.getName()+" "+Register.econ.getVersion()+"!");
                }
            }
            catch (Exception e) {
            }
        }
    }
}
