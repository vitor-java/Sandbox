package net.olokw.sandbox.events;

import com.google.common.reflect.ClassPath;
import net.olokw.sandbox.Sandbox;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Listeners {

    // método para registrar todos os eventos em net.olokw.sandbox.events.

    public static void register() {
        try {
            ClassPath cp = ClassPath.from(Listeners.class.getClassLoader());

            for (ClassPath.ClassInfo classInfo : cp.getTopLevelClassesRecursive("net.olokw.sandbox.events")) {
                Class<?> c = Class.forName(classInfo.getName());
                if (Listener.class.isAssignableFrom(c)) {
                    Listener listener = (Listener) c.getDeclaredConstructor().newInstance();
                    Bukkit.getServer().getPluginManager().registerEvents(listener, Sandbox.instance);
                }
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
