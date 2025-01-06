use lazy_static::lazy_static;
use std::sync::Once;

#[cfg(target_os = "android")]
use {android_logger::Config, log::LevelFilter};

#[cfg(not(target_os = "android"))]
use {env_logger::Builder, log::LevelFilter};

lazy_static! {
    static ref _LOGGER: Once = {
        let init = Once::new();

        #[cfg(target_os = "android")]
        init.call_once(|| {
            #[cfg(not(debug_assertions))]
            let level_filter = LevelFilter::Info;

            #[cfg(debug_assertions)]
            let level_filter = LevelFilter::Debug;

            android_logger::init_once(
                Config::default()
                    .with_tag("Tag_RustAdvtBlocker")
                    .with_max_level(level_filter),
            );
        });

        #[cfg(not(target_os = "android"))]
        init.call_once(|| {
            #[cfg(not(debug_assertions))]
            let level_filter = LevelFilter::Info;

            #[cfg(debug_assertions)]
            let level_filter = LevelFilter::Debug;

            let mut builder = Builder::new();
            builder.filter_level(level_filter);
            builder.init();
        });

        init
    };
}
