# Limits Application for XWiki

Enforce some limits on an XWiki instance: number of users, number of wikis, etc...

* Project Lead: [Guillaume Delhumeau](http://www.xwiki.org/xwiki/bin/view/XWiki/gdelhumeau)
* [Documentation & Download](http://extensions.xwiki.org/xwiki/bin/view/Extension/Limits+Application/)
* [Issue Tracker](http://jira.xwiki.org/browse/LIMITS)
* Communication: [Mailing List](http://dev.xwiki.org/xwiki/bin/view/Community/MailingLists), [IRC](http://dev.xwiki.org/xwiki/bin/view/Community/IRC)
* Minimal XWiki version supported: 7.4
* License: LGPL 2.1
* [Translations](http://l10n.xwiki.org/xwiki/bin/view/Contrib/LimitsApplication)
* Continuous Integration Status: [![Build Status](http://ci.xwiki.org/buildStatus/icon?job=Contrib%20-%20Limits%20Application)](http://ci.xwiki.org/job/Contrib%20-%20Limits%20Application/)

## Developers

### How to build
```
mvn clean install -Pquality --settings maven-settings.xml
```

### Commit new translations
To get the translations done on the [l10n.xwiki.org](http://l10n.xwiki.org/xwiki/bin/view/Contrib/LimitsApplication) website and commit them into the application, you need to execute the `get-translations.sh` command:

```
## Get the translations
./get-translations.sh

## Look at the new translations
git status

## Add changes (example)
git add application-limits-ui/src/main/resources/XWiki/Limits/Translations.fr.xml

## Commit changes
git commit

## Push them (or make a pull request)
git push origin master
```

It should be done before every release.
