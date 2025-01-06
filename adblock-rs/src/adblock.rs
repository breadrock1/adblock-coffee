use adblock::lists::ParseOptions;
use adblock::request::Request;
use adblock::{Engine, FilterSet};

use crate::errors::Result;

pub(crate) struct AdvtBlocker {
    engine: Engine,
}

impl AdvtBlocker {
    pub fn new(filter_list: Vec<String>) -> Self {
        #[cfg(not(debug_assertions))]
        let debug_info = false;

        #[cfg(debug_assertions)]
        let debug_info = true;

        let mut filter_set = FilterSet::new(debug_info);
        filter_set.add_filters(&filter_list, ParseOptions::default());

        let filter_engine = Engine::from_filter_set(filter_set, true);

        AdvtBlocker {
            engine: filter_engine,
        }
    }

    pub fn check_network_urls(&self, url: &str, src_url: &str, req_type: &str) -> Result<bool> {
        let request = Request::new(url, src_url, req_type)?;
        let blocker_result = self.engine.check_network_request(&request);
        Ok(blocker_result.matched)
    }
}

impl Default for AdvtBlocker {
    fn default() -> Self {
        AdvtBlocker::new(Vec::default())
    }
}

unsafe impl Send for AdvtBlocker {}
unsafe impl Sync for AdvtBlocker {}

#[cfg(test)]
mod adblock_test {
    use super::*;

    #[test]
    fn check_base_case() {
        let rules = vec![
            "-advertisement-icon.".to_string(),
            "-advertisement-management/".to_string(),
            "-advertisement.".to_string(),
            "-advertisement/script.".to_string(),
        ];

        let advt_blocker = AdvtBlocker::new(rules);
        let check_result = advt_blocker
            .check_network_urls(
                "http://example.com/-advertisement-icon.",
                "http://example.com/helloworld",
                "image",
            )
            .unwrap();

        assert_eq!(check_result, true);
    }

    #[test]
    fn check_failed_url() {
        let rules = vec![
            "-advertisement-icon.".to_string(),
            "-advertisement-management/".to_string(),
            "-advertisement.".to_string(),
            "-advertisement/script.".to_string(),
        ];

        let advt_blocker = AdvtBlocker::new(rules);
        let check_result = advt_blocker
            .check_network_urls("hvertisement-icon.", "http://exampworld", "kek")
            .unwrap_or_else(|err| {
                log::error!("{:?}", err.to_string());
                false
            });

        assert_eq!(check_result, false);
    }
}
