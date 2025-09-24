# TODO

- Code coverage reports
  - If low create generic task and send it to AI agent to extend the tests

## MVP

- [Onboarding flow](./Tasks/002%20Onboarding/TASK.md)
- Regular HTTP downloads/urls support
- Contacts flow
  - Report problem
  - Request a feature
  - Misc
- Publishing
  - Google Play Store
  - RuStore
  - AppGallery
  - RuMarket

## For 1.0.1

- [Connect with auto-discovery profiles systems (integrate on the back side as well)](./Tasks/004%20Auto-discovery%20profiles%20systems/TASK.md) 
- Sync the user data (WebDAV)
- Premium features
  - Extra theme packs
  - Additional Providers support
  - Tbd
- Publishing
  - Samsung Store
  - Xiaomi
  - F-Droid
  - APKPure
- Support the project / Donations

# For 1.0.2

- Tbd

## FIXME

- None

## In progress

- [Add new automation tests cases into the automation tests](./Tasks/003%20New%20automation%20tests%20to%20add/TASK.md)
- [Enable RAG for AIs and fine tuning](./Tasks/001%20RAG%20and%20MCP%20integration/TASK.md)
  - Move RAG materials into separate Git submodule
    - Submodule Upstreams
    - Private RAG
  - Obtain Android development documentation with wget mirroring
- Upstreams
- Exclude form the IDE indexing Tests directory and RAG materials

## Completed

- https://android-developers.googleblog.com/2025/05/prepare-play-apps-for-devices-with-16kb-page-size.html
- All screens have to be presented between the title bar area and navigation bar
- Prod signing
- Debug build app name suffix
- Crashlytics and general Firebase support
- Tests
- Distribution
- Connect the Toolkit
- Replace use of Android Log with Toolkit Console equivalent
- FIXME: Credentials for Profiles
- Main screen layout - Presenting all available profiles
  - Display in separate section shortcuts for all applications that support our profile schemes
- FIXME: Make sure that all screens fit vertically properly

## Tmp - Backup

Currently we support several types of profiles. However, we have forgotten that for them we may have to define username and password that will be used to authenticate user when communicating with remote endpoint. Please extend profiles which may need username and        │
│   password with this support, make usre that they are optional, and persist them along with other data in Google Room encrypted like we do now. Please extend UI/UX according to the most modern standards for username/password management.

Once you are done please update all of our tests - extend the according to the changes and update all relevant markdown documents in the project.

I am getting error 403 when trying to access the qBitTorrent instance with provided username and password as the defined Profile properties. You MUST check and fix this. Credentials were valid and work with othe application for remote accessing.

Once you are done please check every single supported profile type and if its API is implemented properly. You MUST fix if API communication is not properly implemented or if it is broken. Once done extend and update the tests. Make sure that all tests pass after        │
│   changes. For tests we have run scripts in the root of the project.

After you have done this, update the main screen which is scrollable to have icon of each created profile with possibility of opening it like we can now with the default one. Default item can have the golden start as indicator. Add lock indicator for the profiles        │
│   with assigned credentials used for access. On long press add options - for now we need edit option exposed. Design must be like so far modern and smooth! Make sure that everything works nicely and after you are done check and update tests and markdowns if needed.        │
│                                                                                                                                                                                                                                                                                  │
│   And finally once main screen is uodated, after all profiles present another set of icons with similar approach - present one after another shortcuts for opening all system applications that can handle data types that our profiles can handle: youtube and other            │
│   streaming services links, torrent files and magnet links, jdownloader compatible urls, etc. Add proper header so user understands taht these applications are part of the system, not the remote endpoints that our Profiles are covering.                                      │
│                                                                                                                                                                                                                                                                                  │
│   Next, we have situtation that not all activites fir between the title bar and navigation bar (if any). We MUST fix this! All screens must satisfy proper display! Once fix is applied extend all automation tests to verify this case! Note: There are already some            │
│   screens where we have fixed/applied this. However, as I have already explained, not all screens follow this.

Please extend the implementation with this point as well:                                                                                                                                                                                                                      │
│                                                                                                                                                                                                                                                                                  │
│   - Add title and description for history list items                                                                                                                                                                                                                             │
│                                                                                                                                                                                                                                                                                  │
│   So basically for shared item to particular Profile obtain title and description if possible. Thumbnail would be great too and use it in shared item presentation. So basically we would not have just the URL.                                                                 │
│                                                                                                                                                                                                                                                                                  │
│   Extend tests to test this changes too and make sure that we are passing them by running our run tests scipts.                                                                                                                                                                  │
│                                                                                                                                                                                                                                                                                  │
│   At the end update all markdowns where it is needed (if anywhere)    