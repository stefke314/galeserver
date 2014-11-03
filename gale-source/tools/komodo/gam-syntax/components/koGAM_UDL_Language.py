# Komodo GAM language service.

import logging
from koUDLLanguageBase import KoUDLLanguage


log = logging.getLogger("koGAMLanguage")
#log.setLevel(logging.DEBUG)


def registerLanguage(registry):
    log.debug("Registering language GAM")
    registry.registerLanguage(KoGAMLanguage())


class KoGAMLanguage(KoUDLLanguage):
    name = "GAM"
    lexresLangName = "GAM"
    _reg_desc_ = "%s Language" % name
    _reg_contractid_ = "@activestate.com/koLanguage?language=%s;1" % name
    _reg_clsid_ = "bd3f14e5-71cb-453a-a21e-9f971aaed934"
    defaultExtension = '.gam'

    #TODO: Update 'lang_from_udl_family' as appropriate for your
    #      lexer definition. There are four UDL language families:
    #           M (markup), i.e. HTML or XML
    #           CSL (client-side language), e.g. JavaScript
    #           SSL (server-side language), e.g. Perl, PHP, Python
    #           TPL (template language), e.g. RHTML, Django, Smarty
    #      'lang_from_udl_family' maps each UDL family code (M,
    #      CSL, ...) to the sub-langauge name in your language.
    #      Some examples:
    #        lang_from_udl_family = {   # A PHP file can contain
    #           'M': 'HTML',            #   HTML
    #           'SSL': 'PHP',           #   PHP
    #           'CSL': 'JavaScript',    #   JavaScript
    #        }
    #        lang_from_udl_family = {   # An RHTML file can contain
    #           'M': 'HTML',            #   HTML
    #           'SSL': 'Ruby',          #   Ruby
    #           'CSL': 'JavaScript',    #   JavaScript
    #           'TPL': 'RHTML',         #   RHTML template code
    #        }
    #        lang_from_udl_family = {   # A plain XML can just contain
    #           'M': 'XML',             #   XML
    #        }
    lang_from_udl_family = {
    	'M': 'GAM',
    }
