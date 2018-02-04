# serposcope changelog

## 2.9 - UNRELEASED

## 2.8 - 2017-11-23

* Fix TLD/country localization issue. TLD is replace by country. On upgrade, TLD are automatically converted to associated country unless it is a generic TLD. You may want to reconfigure your default country for new keyword in admin > settings > google > country.
* Scraper module sources published to maven (to be moved in another repo in near futur)

## 2.7.1 - 2017-03-24

* IMPORTANT Fix new recaptcha v2 form issue [#155](https://github.com/serphacker/serposcope/issues/155)
* Big improvements on captcha handling and rate limiting
* SERP parsing : exclude sitelinks from rankings, expect ranking variation
* Better IDN support [#130](https://github.com/serphacker/serposcope/issues/130)
* Hide add event button if not admin [#133](https://github.com/serphacker/serposcope/issues/133)
* Fix issue with .com TLD
* Fix CSV export issue [#135](https://github.com/serphacker/serposcope/issues/135)
* Fix unrecognized SSL certificat issue
* German translation thanks to [@stritti](https://github.com/stritti)

## 2.6.0 - 2016-09-29

* IMPORTANT Fix captcha form issue [#132](https://github.com/serphacker/serposcope/issues/132)
* Fix NPE in log anonymizer [#127](https://github.com/serphacker/serposcope/issues/127)
* Fix mobile icon no more displayed [#128](https://github.com/serphacker/serposcope/issues/128)
* Fix export date range selection
* Increased captcha retry on failure (service overload) from 3 to 5

## 2.5.1 - 2016-07-10

* Fix escaping issue on H2 database export (backup feature)

## 2.5.0 - 2016-07-07

* Can check only failed keywords or recently added keywords [#96](https://github.com/serphacker/serposcope/issues/96)
* Reworked most of the views to support huge amount of keywords (hundred of thousands...)
* Export: can export SERP or rankings in CSV
* Captcha Failover: Ability to configure multiple captcha service, if one service fail, serposcope fallback to others providers
* Optimized SERP rescan speed : adding a website should be 20x faster (usefull when having thousands of keywords)
* Can backup and restore the database from admin panel
* Can migrate easily between H2 <-> MySQL using the new backup/restore feature
* Database prunning : trim database & history, permit to limit database disk usage. Defaulted to 365 days.
* New scoring system (all scores have been reset)
* Use a new smartphone user-agent for mobile SERP result
* improved SERP parsing, Google news div is no more parsed
* Websites bulk import and bulk delete
* Can rename website
* New default search settings is 1 page of 100 results and 5 sec. pause (was 5x10 and 10 sec. pause, now 20x faster)
* Can add event/calendar on search and target view
* Warn if tracking too many keywords using H2 database
* Group view: Can sort websites
* Group view: Display total number of keywords
* Group view: Grid display for keywords, support thousands of keywords
* Group view: Allow sorting & filtering of keywords, fix [#59](https://github.com/serphacker/serposcope/issues/59)
* Target chart view: Fix bugged legend
* Target chart view: Do not draw automatically charts when too many keywords
* Target variation view: Improved date-range picker (related to [#114](https://github.com/serphacker/serposcope/issues/114))
* Target variation and table view: Grid display, support thousands of keywords
* Target variation and table view: Allow sorting & filtering
* Search SERP view: FIX huge legend, can draw top10 on chart in one click [#66](https://github.com/serphacker/serposcope/issues/66)
* Search SERP view: Display best ranking for tracked website
* Homepage: Display DB disk usage (H2 only) and remaining free disk space
* Homepage: Enhanced TOP keywords count using % and better UI
* Homepage: Progress bar is now dynamic via ajax (no need to refresh) [#90](https://github.com/serphacker/serposcope/issues/90)
* Homepage: Display last runs state [#83](https://github.com/serphacker/serposcope/issues/83)
* Homepage: Can cancel current runs or view logs from homepage
* FIX incorrect random pause between request to Google[#100](https://github.com/serphacker/serposcope/issues/100)
* FIX local suggest case
* FIX httpclient support defalte compression and minor improvements
* FIX duplicate keywords on bulk import (related to [#59](https://github.com/serphacker/serposcope/issues/59))
* FIX MySQL: charset issue when using and not UTF-8 [#112](https://github.com/serphacker/serposcope/issues/112) [#115](https://github.com/serphacker/serposcope/issues/115)
* FIX MySQL: no more case sensitive and fix accent issue
* FIX admin users table layout [#102](https://github.com/serphacker/serposcope/issues/102)
* FIX calendar bug on chart redraw
* FIX invalid timeout on captcha service
* FIX SERP chart display rank 0 for unranked position

## 2.4.0 - 2016-03-17

* IMPORTANT Google added a new "q" parameter to its captcha form (upgrade highly recommended).
* [Docker image available](https://github.com/serphacker/serposcope/tree/master/docker) thanks to @pierreavizou [#43](https://github.com/serphacker/serposcope/issues/43)

## 2.3.0 - 2016-02-15

* IMPORTANT Google task crash when checking with multiple threads [#82](https://github.com/serphacker/serposcope/issues/82)

## 2.2.0 - 2016-02-11

* IMPORTANT FIX task may not stop when using proxies [#80](https://github.com/serphacker/serposcope/issues/80)
* comptabile with 64 bits Java version on Windows
* support SOCKS proxy [#79](https://github.com/serphacker/serposcope/issues/79)
* best ranking is next to last ranking (website table view) [#50](https://github.com/serphacker/serposcope/issues/50)
* ability to delete invalid proxy in one click
* display number of captchas displayed even without solver [#54](https://github.com/serphacker/serposcope/issues/54)
* ability to do a stackdump (debugging utility)
* FIX bulk import bug with canonical location [#63](https://github.com/serphacker/serposcope/issues/63)
* FIX javascript when using quotes in website name or search [#81](https://github.com/serphacker/serposcope/issues/81)
* FIX typos [#74](https://github.com/serphacker/serposcope/issues/74)

## 2.1.0 - 2016-01-15

* add https://anti-captcha.com/ captcha service [#58](https://github.com/serphacker/serposcope/issues/58)
* add http://de-captcher.com/ captcha service [#58](https://github.com/serphacker/serposcope/issues/58)
* ability to check captcha balance from settings
* randomized keywords before checking  [#40](https://github.com/serphacker/serposcope/issues/40)
* improve keyword bulk import speed
* Windows binaries are signed
* FIX Avast false positive
* FIX incorrect .deb packages [#47](https://github.com/serphacker/serposcope/issues/47)
* FIX invalid log link on home

## 2.0.0 - 2016-01-05

To view list of change since version 1 please read : https://serphacker.com/en/blog/whats-new-in-serposcope-2.html