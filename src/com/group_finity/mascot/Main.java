package com.group_finity.mascot;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.sun.jna.Platform;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;



/**
 * プログラムのエントリポイント.
 */
public class Main {

	private static final Logger log = Logger.getLogger(Main.class.getName());

	static final String BEHAVIOR_GATHER = "マウスの周りに集まる";

	static {
		try {
			LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
		} catch (final SecurityException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private static Main instance = new Main();

	public static Main getInstance() {
		return instance;
	}

	private final Manager manager = new Manager();

	private final Configuration configuration = new Configuration();

	public static void main(final String[] args) {

		getInstance().run();
	}

	public void run() {

		// 設定を読み込む
		loadConfiguration();

		// トレイアイコンを作成する
		createTrayIcon();

		// しめじを一匹作成する
		createMascot();

		getManager().start();
	}


	/**
	 * 設定ファイルを読み込む.
	 */
	private void loadConfiguration() {

		try {
			log.log(Level.INFO, "設定ファイルを読み込み({0})", "/動作.xml");

			final Document actions = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
					Main.class.getResourceAsStream("/動作.xml"));

			log.log(Level.INFO, "設定ファイルを読み込み({0})", "/行動.xml");

			this.getConfiguration().load(new Entry(actions.getDocumentElement()));

			final Document behaviors = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
					Main.class.getResourceAsStream("/行動.xml"));

			this.getConfiguration().load(new Entry(behaviors.getDocumentElement()));

			this.getConfiguration().validate();

		} catch (final IOException e) {
			log.log(Level.SEVERE, "設定ファイルの読み込みに失敗", e);
			exit();
		} catch (final SAXException e) {
			log.log(Level.SEVERE, "設定ファイルの読み込みに失敗", e);
			exit();
		} catch (final ParserConfigurationException e) {
			log.log(Level.SEVERE, "設定ファイルの読み込みに失敗", e);
			exit();
		} catch (final ConfigurationException e) {
			log.log(Level.SEVERE, "設定ファイルの記述に誤りがあります", e);
			exit();
		}
	}

	/**
	 * トレイアイコンを作成する.
	 * @throws AWTException
	 * @throws IOException
	 */
	private void createTrayIcon() {

		log.log(Level.INFO, "トレイアイコンを作成");

		if ( SystemTray.getSystemTray()==null ) {
			return;
		}

		// 「増やす」メニューアイテム
		final MenuItem increaseMenu = new MenuItem("増やす");
		increaseMenu.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				createMascot();
			}
		});

		// 「あつまれ！」メニューアイテム
		final MenuItem gatherMenu = new MenuItem("あつまれ！");
		gatherMenu.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				gatherAll();
			}
		});

		// 「一匹だけ残す」メニューアイテム
		final MenuItem oneMenu = new MenuItem("一匹だけ残す");
		oneMenu.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				remainOne();
			}
		});

		// 「IEを元に戻す」メニューアイテム
		final MenuItem restoreMenu =
			new MenuItem(Platform.isMac() ? "ウィンドウを元に戻す" : "IEを元に戻す");
		restoreMenu.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				restoreIE();
			}
		});

		// 「ばいばい」メニューアイテム
		final MenuItem closeMenu = new MenuItem("ばいばい");
		closeMenu.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				exit();
			}
		});

		// ポップアップメニューを作成
		final PopupMenu trayPopup = new PopupMenu();
		trayPopup.add(increaseMenu);
		trayPopup.add(gatherMenu);
		trayPopup.add(oneMenu);
		trayPopup.add(restoreMenu);
		trayPopup.add(new MenuItem("-"));
		trayPopup.add(closeMenu);

		try {
			// トレイアイコンを作成
			final TrayIcon icon = new TrayIcon(ImageIO.read(Main.class.getResource("/icon.png")), "しめじ", trayPopup);

            // アイコンが左クリックされたときも「増える」
			// ただし Mac では左クリックでメニューが開いてしまうため、
			// 右クリックで増殖させる
			icon.addMouseListener(new MouseAdapter() {
        @Override
				public void mouseClicked(final MouseEvent e) {
					if (!Platform.isMac() && SwingUtilities.isLeftMouseButton(e)
							|| Platform.isMac() && SwingUtilities.isRightMouseButton(e)) {
						createMascot();
					}
				}
			});

			// トレイアイコンを表示
			SystemTray.getSystemTray().add(icon);

		} catch (final IOException e) {
			log.log(Level.SEVERE, "トレイアイコンの作成に失敗", e);
			exit();

		} catch (final AWTException e) {
			log.log(Level.SEVERE, "トレイアイコンの作成に失敗", e);
			MascotEventHandler.setShowSystemTrayMenu(true);
			getManager().setExitOnLastRemoved(true);
		}

	}

	/**
	 * しめじを一匹作成する.
	 */
	public void createMascot() {

		log.log(Level.INFO, "マスコットを作成");

		// マスコットを1個作成
		final Mascot mascot = new Mascot();

		// 範囲外から開始
		mascot.setAnchor(new Point(-1000, -1000));
		// ランダムな向きで
		mascot.setLookRight(Math.random() < 0.5);

		try {
			mascot.setBehavior(getConfiguration().buildBehavior(null, mascot));

			this.getManager().add(mascot);

		} catch (final BehaviorInstantiationException e) {
			log.log(Level.SEVERE, "最初の行動の初期化に失敗しました", e);
			mascot.dispose();
		} catch (final CantBeAliveException e) {
			log.log(Level.SEVERE, "生き続けることが出来ない状況", e);
			mascot.dispose();
		}

	}

	public void gatherAll() {
		Main.this.getManager().setBehaviorAll(Main.this.getConfiguration(), BEHAVIOR_GATHER);
	}

	public void remainOne() {
		Main.this.getManager().remainOne();
	}

	public void restoreIE() {
		NativeFactory.getInstance().getEnvironment().restoreIE();
	}

	public void exit() {

		this.getManager().disposeAll();
		this.getManager().stop();

		System.exit(0);
	}

	public Configuration getConfiguration() {
		return this.configuration;
	}

	private Manager getManager() {
		return this.manager;
	}

}
