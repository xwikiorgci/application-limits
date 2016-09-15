#!/bin/bash
USER="$L10N_USER"
PASS="$L10N_PASSWORD"

PROJECT_TRUNKS=`pwd`

function download_translations() {
    wget $1 --user="${USER}" --password="${PASS}" --auth-no-challenge -O ./translations.zip ##&&
    unzip -o translations.zip &&
    rm translations.zip || $(git clean -dxf && exit -1)
}

function read_user_and_password() {
    if [[ -z "$USER" || -z "$PASS" ]]; then
        echo -e "\033[0;32mEnter your l10n.xwiki.org credentials:\033[0m"
        read -e -p "user> " USER
        read -e -s -p "pass> " PASS
        echo ""
    fi

    if [[ -z "$USER" || -z "$PASS" ]]; then
      echo -e "\033[1;31mPlease provide both user and password in order to be able to get the translations from l10n.xwiki.org.\033[0m"
      exit -1
    fi
}

##
## Download the translations and format them.
## Note: it is the responsibility of the developer to actually commit them.
##
read_user_and_password
cd ${PROJECT_TRUNKS}/application-limits-api/src/main/resources/  || exit -1
download_translations 'http://l10n.xwiki.org/xwiki/bin/view/L10NCode/GetTranslationFile?name=Contrib.LimitsApplication&app=Contrib'
cd ${PROJECT_TRUNKS}
