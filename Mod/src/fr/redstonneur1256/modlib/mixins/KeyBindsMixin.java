//package fr.redstonneur1256.modlib.mixins;
//
//import arc.Core;
//import arc.Input;
//import arc.input.InputMultiplexer;
//import arc.input.InputProcessor;
//import arc.input.KeyBind;
//import arc.input.KeyCode;
//import arc.input.KeyboardDevice;
//import arc.math.Mathf;
//import arc.struct.ObjectMap;
//import arc.struct.OrderedMap;
//import fr.redstonneur1256.modlib.key.KeyBindAccessor;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import java.util.Arrays;
//
//@Mixin(KeyBind.class)
//public abstract class KeyBindsMixin implements KeyBindAccessor {
//
//    private @Shadow ObjectMap<KeyBind, ObjectMap<InputDevice.DeviceType, KeyBind.Axis>> defaultCache;
//    private @Shadow KeyBind[] definitions;
//    private @Shadow KeyBinds.Section[] sections;
//    private @Unique boolean loaded;
//
//    @Inject(method = "setDefaults", at = @At("RETURN"))
//    private void setDefaults(KeyBind[] defaults, KeyBinds.Section[] sectionArr, CallbackInfo ci) {
//        // Since setDefault is called with Binding.values() the array is an array of Binding inside which we cannot store
//        // our own keybindings, this forces it to be a KeyBind array
//
//        definitions = new KeyBind[defaults.length];
//        System.arraycopy(defaults, 0, definitions, 0, defaults.length);
//    }
//
//    @Inject(method = "load()V", at = @At("TAIL"))
//    private void load(CallbackInfo ci) {
//        loaded = true;
//    }
//
//    @Override
//    public void mindustry_ModLib$registerKeyBinds(KeyBind[] binds) {
//        int previousLength = definitions.length;
//        definitions = Arrays.copyOf(definitions, previousLength + binds.length);
//        System.arraycopy(binds, 0, definitions, previousLength, binds.length);
//
//        for (KeyBind def : binds) {
//            defaultCache.put(def, new ObjectMap<>());
//            for (InputDevice.DeviceType type : InputDevice.DeviceType.values()) {
//                KeyBind.Axis axis = def.defaultValue instanceof KeyBind.Axis ?
//                        (KeyBind.Axis) def.defaultValue :
//                        new KeyBind.Axis((KeyCode) def.defaultValue);
//
//                defaultCache.get(def).put(type, axis);
//            }
//        }
//
//        if (loaded) {
//            // Same code than the #load() method but instead of looping through all definitions we only loop through the new ones
//            for (KeyBinds.Section section : sections) {
//                for (InputDevice.DeviceType type : InputDevice.DeviceType.values()) {
//                    for (KeyBind bind : binds) {
//                        String name = "keybind-" + section.name + "-" + type.name() + "-" + bind.name();
//
//                        KeyBind.Axis loaded = load(name);
//                        if (loaded != null) {
//                            section.binds.get(type, OrderedMap::new).put(bind, loaded);
//                        }
//                    }
//                }
//
//                int deviceIndex = Mathf.clamp(Core.settings.getInt(section.name + "-last-device-type", 0), 0, Core.input.getDevices().size - 1);
//                section.device = Core.input.getDevices().get(deviceIndex);
//            }
//        }
//    }
//
//    @Shadow
//    protected abstract KeyBind.Axis load(String name);
//
//}
