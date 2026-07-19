# 2.3.6 - 2026/07/19

### Changed
- Mobile UI redesigned to feel closer to Reddit web on mobile:
  - Reddit orange brand colors (light + dark themes)
  - Full-width post cards on a gray canvas with 8dp gaps between posts
  - Compact single-line post header: `r/subreddit · u/author · time`
  - Title above media; full-bleed images without heavy rounded corners
  - Denser typography and action bar (votes / comments / save)
  - Full-width labeled bottom navigation that slides down on scroll (instead of the floating side pill)

# 2.3.5 - 2026/07/12

### Added
- "Full images in feed" setting: show feed images at their full size instead of the cropped thumbnail (off by default, toggle in Settings > Content)

### Removed
- The "might stop working soon" warning banner

# 2.3.4 - 2026/07/12

### Changed
- Images now display at their real aspect ratio instead of being cropped to a fixed box, both in the feed and on the post screen
- The post screen's top bar now shows the subreddit (e.g. r/pics) so it's clear where you are

# 2.3.3 - 2026/07/12

### Changed
- More resilient Reddit connection: the anonymous token is now requested with a rotating pool of Reddit for Android user agents and retried with backoff (including on rate limits), reducing "Something went wrong" errors

# 2.3.2 - 2026/07/12

### Fixed
- Reddit feeds not loading ("Something went wrong"): the app now authenticates against the official Reddit API with an anonymous token, since Reddit no longer serves its public .json endpoints to third-party clients
- Reddit scraping source being blocked because of the default HTTP client user agent

# 2.3.1 - 2025/06/01

### Fixed
- Redgifs videos

# 2.3.0 - 2023/08/15

This version is only compatible with Android 6.0 onwards.

### Added
- Monochrome icon

### Changed
- Improve NSFW consent when scraping
- Minimum Android version (now Android 6.0)

### Fixed
- App not starting on Android 14
- Email intent on About page

# 2.2.2 - 2023/07/30

### Fixed
- Missing audio in Reddit videos

# 2.2.1 - 2023/06/05

### Fixed
- Missing ratio in post details

# 2.2.0 - 2023/06/04

### Added
- Web Scraping source (based on old.reddit.com)
- Disclaimer for Reddit's API policy change

# 2.1.1 - 2023/03/18

### Added
- More Teddit API endpoints

### Fixed
- Redgifs videos

# 2.1.0 - 2022/12/28

### Added
- Privacy Enhancer: redirect services to privacy-friendly frontends
- More Teddit instances

### Changed
- Automatically upgrade media links to HTTPS

### Fixed
- Various bugs

# 2.0.3 - 2022/12/13

### Fixed
- Fix unselectable backup files when using the import feature

# 2.0.2 - 2022/12/10

### Fixed
- Crashes with blank profile names

# 2.0.1 - 2022/11/28

### Changed
- Better sorting when having more than 100 subscriptions

### Fixed
- Endless loading in case of errors
- Padding on home screen

# 2.0.0 - 2022/11/26

This includes changes from previous alpha versions.

### Added
- Teddit API: choose between Reddit official API and Teddit API to fetch data (experimental)
- Amoled theme
- Import/Export feature: back up and restore data from Stealth or Reddit
- Profile Switcher on home screen
- Pull to refresh
- Support for more than 100 subscriptions

### Changed
- Improved performances
- Various UI improvements

### Removed
- In-app browser: replace with CustomTab

### Fixed
- Some crashes and UI bugs

# 2.0.0-alpha04 - 2022/09/10

### Added
- Support for body text in all posts
- Log crashes to a file on external storage

### Changed
- Moved bottom navigation bar to the side (with left-handed support)

### Fixed
- Redgifs videos (temporary fix)
- Crashes when domain or url is null in a post

# 2.0.0-alpha03 - 2022/04/17

### Fixed
- Crashes when restoring backups

# 2.0.0-alpha02 - 2022/04/17

### Added
- Import/Export feature: back up and restore data from Stealth or Reddit

### Fixed
- Discrepancies in comments when leaving and coming back to post detail
- Bottom navigation not hiding on scroll in Post List
- Crashes when clicking on poll links (thanks to @mwiggins)

# 2.0.0-alpha01 - 2021/12/13

### Added
- Teddit API: choose between Reddit official API and Teddit API to fetch data (experimental)
- Amoled theme
- Incognito mode by default for keyboard

### Changed
- Improved performances
- Various UI updates

### Removed
- In-app browser: replace with CustomTab
- Max limit for search queries

### Fixed
- Unordered images in galleries
- Comments not saved on configuration change
- Banner not collapsing when navigating to/from Subreddits

# 1.1.3 - 2021/10/25

### Fixed
- Crashes on Android 12+

# 1.1.2 - 2021/08/28

### Added
- Support for redd.it links

### Fixed
- Redgifs videos
- Reddit permalinks opened as external links

# 1.1.1 - 2021/08/19

### Added
- Support for physical keyboards

### Fixed
- NSFW content not showing in search results
- Reddit galleries not loading in saved posts
- Crashes with some user and Subreddit links

# 1.1.0 - 2021/07/17

### Added
- Built-in browser
- Media downloader: save images and videos from posts
- Ability to rename profiles

### Changed
- Improve navigation

### Fixed
- Crashes on some Subreddits
- Wiki links opened as Subreddit links

# 1.0.1 - 2021/05/09

### Fixed
- Fix external links on Android 11+

# 1.0.0 - 2021/05/09

### Added
- Profiles: create different profiles to have distinct subscriptions, history and saved items
- Support for crossposts
- Ability to save posts and comments
- Suggested sort
- Mute button in Media Viewer
- Clickable links in tables
- German translation (thanks to @uDEV2019)
- Spanish translation (thanks to @another-sapiens)

### Changed
- Open drawer more easily on a Subreddit page

# 0.1.1 - 2021/04/06

### Fixed
- Fix Reddit videos not playing when audio is not found
- Fix crash when opening Subreddit from user page
- Fix share feature
- Fix wrong search query being displayed in search page
- Fix for some Gfycat videos not playing

# 0.1.0 - 2021/03/27

### Added
- Subscriptions
- Search
- Sort
- History
- NSFW filter
- Flairs
- Awards
- Light/Dark theme
