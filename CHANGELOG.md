# serposcope changelog

## 2.5.0 - UNRELEASED

* FIX incorrect random pause between request to Google[#100](https://github.com/serphacker/serposcope/issues/100)

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