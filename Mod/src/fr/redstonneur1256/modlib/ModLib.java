package fr.redstonneur1256.modlib;

import arc.Core;
import arc.Events;
import arc.func.Floatp;
import arc.graphics.g2d.Draw;
import arc.scene.ui.Dialog;
import arc.scene.ui.Label;
import arc.scene.ui.ProgressBar;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Log;
import arc.util.Threads;
import arc.util.Time;
import fr.redstonneur1256.modlib.launcher.JreDownloader;
import fr.redstonneur1256.modlib.launcher.LauncherInitializer;
import fr.redstonneur1256.modlib.launcher.ModLibLauncher;
import fr.redstonneur1256.modlib.launcher.log.Logger;
import fr.redstonneur1256.modlib.net.MNet;
import fr.redstonneur1256.modlib.ui.MUI;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class ModLib extends Mod {

    public ModLib() {
        if (JreDownloader.isJreDownloaded())
            LauncherInitializer.initialize();
        else
            Events.on(EventType.ClientLoadEvent.class,
                    event -> Time.runTask(10f, ModLib::showJreDownloadSuggestionMenu));
    }

    @Override
    public void init() {
        if (!LauncherInitializer.isInitialized()) {
            return;
        }


        MVars.net = new MNet();
        if (!Vars.headless) {
            MVars.ui = new MUI();
        }
    }

    public static String getVersion() {
        return Vars.mods.getMod(ModLib.class).meta.version;
    }

    public static void restartOnExit(boolean reloadLauncher) {
        ModLibLauncher.launcher.restartGame = true;
        ModLibLauncher.launcher.fastRestart = !reloadLauncher;
    }

    private static void showJreDownloadSuggestionMenu() {
        BaseDialog dialog = new BaseDialog("[ModLib (UNOFFICIAL)] Standalone Java Required");
        dialog.cont.add("To make ModLib and all mods depending on it work please download official Java 25 Redistributable").pad(20);
        dialog.cont.row();
        dialog.buttons.button("Why?", ModLib::showIntentionsDialog).size(120, 50).pad(10);

        dialog.buttons.button("Download", () -> {
            try {
                dialog.hide();
                showProgressDialog(dialog);
                JreDownloader.checkAndDownloadJRE();
            } catch (Exception e) {
                Log.err(e.getMessage());
            }
        }).size(160f, 50f).pad(10);

        dialog.buttons.button("Cancel", () -> {
            showCancelWarningDialog(dialog);
        }).size(120, 50).pad(10);

        dialog.show();
    }

    private static void showIntentionsDialog() {
        Dialog whyDialog = new Dialog("Information");

        Label info = new Label("""
                This application requires a standalone Java 25 runtime environment (JRE).
                Older system installations of Java are incompatible with modern systems.
                Without this 50MB package, game elements, script injections, and mod
                dependencies will fail to launch entirely
                The package is fetched securely from official Adoptium APIs.
            """);

        whyDialog.cont.add(info).pad(30).row();

        whyDialog.buttons.button("Done", whyDialog::hide).size(120, 50).pad(10);
        whyDialog.show();
    }

    private static void showCancelWarningDialog(BaseDialog dialog) {
        Dialog warningDialog = new Dialog("Warning!");
        Label info = new Label("""
                Without downloading this package dependant mods will be inactive.
                This dialog will open again on relaunch if you don't want to download Java now.
                """);
        warningDialog.cont.add(info).pad(30);

        warningDialog.buttons.button("Ok", () -> {
            warningDialog.hide();
            dialog.hide();
        }).size(120, 50).pad(10);

        warningDialog.buttons.button("Cancel", warningDialog::hide).size(120, 50).pad(10);

        warningDialog.show();
    }

    private static void showProgressDialog(BaseDialog dialog) {
        Dialog progressDialog = new Dialog("Downloading Java 25 JRE");
        Label progressLabel = new Label("...");

        progressDialog.cont.add(progressLabel).pad(30).row();
        Bar bar = progressDialog.cont.add(new Bar()).size(500,40).pad(30).get();
        progressDialog.cont.row();
        progressDialog.buttons.button("Done (No Changes)", progressDialog::hide).size(120, 50).disabled(true).pad(10);
        progressDialog.buttons.button("Restart game", () ->
        {
            progressDialog.hide();
            Time.runTask(20, LauncherInitializer::initialize);
        }).size(120, 50).disabled(true).pad(10);
        progressDialog.show();

        Threads.daemon(() -> {
            JreDownloader.DownloadStatus.State prevState = null;
            while (JreDownloader.DownloadStatus.isDownloading()) {
                if (prevState != JreDownloader.DownloadStatus.state)
                    progressLabel.setText(JreDownloader.DownloadStatus.state.toString());
                prevState = JreDownloader.DownloadStatus.state;
                Floatp progress = () -> JreDownloader.DownloadStatus.installationProgress;
                bar.set(() -> (int)(progress.get() * 100f) + "%", progress, Pal.accent);
            }
            progressLabel.setText(JreDownloader.DownloadStatus.state.toString());
            bar.setColor(JreDownloader.DownloadStatus.isError() ? Pal.health : Pal.heal);
            if (JreDownloader.DownloadStatus.isError())
                bar.set(() -> JreDownloader.DownloadStatus.errMessage, null, Pal.health);
            for (var button : progressDialog.buttons.getCells())
                button.disabled(false);
        });
    }
}
