require(['jquery', 'xwiki-meta'], function ($, xm) {
  'use strict';

  $(document).ready(function () {
    // If this JS is loaded, it means that the user has reached the user limits.
    // So we remove the 'Add user' button and we replace it by a message.
    $('#tdAddNewUserOrGroup .buttonwrapper').remove();
    $('#tdAddNewUserOrGroup').append(
      $("<div class=\"box warningmessage\">$escapetool.javascript($escapetool.xml($services.localization.render('limits.user.reachedLimit')))</div>")
    );

    // Disable the create wiki form
    if (xm.document == 'WikiManager.CreateWiki') {
      $('#xwikicontent > *').remove();
      $('#xwikicontent').append(
        $("<div class=\"box errormessage\">$escapetool.javascript($escapetool.xml($services.localization.render('limits.wikis.createwiki', [$services.limits.wikiLimit])))</div>")
      );
    }

    // Disable the registration form
    if (xm.document == 'XWiki.XWikiRegister') {
      $('#mainContentArea > *').remove();
      $('#mainContentArea').append(
        $("<div class=\"box errormessage\">$escapetool.javascript($escapetool.xml($services.localization.render('limits.user.reachedLimit')))</div>")
      );
    }
  });

});
