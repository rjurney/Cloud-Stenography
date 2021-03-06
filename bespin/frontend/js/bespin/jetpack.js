/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and
 * limitations under the License.
 *
 * The Original Code is Bespin.
 *
 * The Initial Developer of the Original Code is Mozilla.
 * Portions created by the Initial Developer are Copyright (C) 2009
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Bespin Team (bespin@mozilla.com)
 *
 * ***** END LICENSE BLOCK ***** */

dojo.provide("bespin.jetpack");

/**
 * Jetpack Plugin
 * <p>The Jetpack plugin aims to make Bespin a good environment for creating and
 * hosting Jetpack extensions.
 * <p>Read more about Jetpack at: https://wiki.mozilla.org/Labs/Jetpack/API
 */
bespin.jetpack.projectName = "jetpacks";

/**
 * Command store for the Jetpack commands
 * (which are subcommands of the main 'jetpack' command)
 */
bespin.jetpack.commands = new bespin.command.Store(bespin.command.store, {
    name: 'jetpack',
    preview: 'play with jetpack features',
    completeText: 'jetpack subcommands:<br><br> create [name] [type]<br> install [name]<br> list<br> edit [name]',
    subcommanddefault: 'help'
});

/**
 * 'jetpack help' command
 */
bespin.jetpack.commands.addCommand({
    name: 'help',
    takes: ['search'],
    preview: 'show commands for jetpack subcommand',
    description: 'The <u>help</u> gives you access to the various commands in the Bespin system.<br/><br/>You can narrow the search of a command by adding an optional search params.<br/><br/>Finally, pass in the full name of a command and you can get the full description, which you just did to see this!',
    completeText: 'optionally, narrow down the search',
    execute: function(instruction, extra) {
        var output = this.parent.getHelp(extra, {
            suffix:  "For more info and help on the available API, <a href='https://wiki.mozilla.org/Labs/Jetpack/API'>check out the Reference</a>"
        });
        instruction.addOutput(output);
    }
});

/**
 * 'jetpack create' command
 */
bespin.jetpack.commands.addCommand({
    name: 'create',
    takes: ['feature', 'type'],
    preview: 'create a new jetpack feature of the given type (defaults to sidebar)',
    description: 'Create a new jetpack feature that you can install into Firefox with the new Jetpack goodness.',
    completeText: 'name of your feature, type of JetPack template (sidebar, content, toolbar)',
    execute: function(instruction, opts) {
        var feature = opts.feature || 'newjetpack';
        var type = opts.type || 'sidebar';
        var project = bespin.jetpack.projectName;
        var filename = feature + ".js";

        var templateOptions = {
            stdtemplate: "jetpacks/" + type + ".js",
            values: {
                templateName: feature
            }
        };

        bespin.get("server").fileTemplate(project,
            filename,
            templateOptions,
            {
                onSuccess: function(xhr) {
                    bespin.publish("editor:openfile", {
                        project: project,
                        filename: filename
                    });
                },
                onFailure: function(xhr) {
                    instruction.addErrorOutput("Unable to create " + filename + ": " + xhr.responseText);
                }
            }
        );

    }
});

/**
 * 'jetpack install' command
 */
bespin.jetpack.commands.addCommand({
    name: 'install',
    takes: ['feature'],
    preview: 'install a jetpack feature',
    description: 'Install a Jetpack feature, either the current file, or the named feature',
    completeText: 'optionally, the name of the feature to install',
    execute: function(instruction, feature) {
        // For when Aza exposes the Jetpack object :)
        // if (!window['Jetpack']) {
        //     bespin.get("commandLine").addErrorOutput("To install a Jetpack, you need to have installed the extension.<br><br>For now this lives in Firefox only, and you can <a href='https://wiki.mozilla.org/Labs/Jetpack/API'>check it out, and download the add-on here</a>.");
        //     return;
        // }

        // Use the given name, or default to the current jetpack
        feature = feature || (function() {
            var editSession = bespin.get('editSession');
            if (editSession.project != bespin.jetpack.projectName) return; // jump out if not in the jetpack project
            var bits = editSession.path.split('.');
            return bits[bits.length - 2];
        })();

        if (!feature) {
            bespin.get("commandLine").addErrorOutput("Please pass in the name of the Jetpack feature you would like to install");
        } else {
            bespin.jetpack.install(feature);
        }
    }
});

/**
 * 'jetpack list' command
 */
bespin.jetpack.commands.addCommand({
    name: 'list',
    preview: 'list out the Jetpacks that you have written',
    description: 'Which Jetpacks have you written and have available in BespinSettings/jetpacks. NOTE: This is not the same as which Jetpacks you have installed in Firefox!',
    execute: function(instruction, extra) {
        bespin.get('server').list(bespin.jetpack.projectName, '', function(jetpacks) {
            var output;

            if (!jetpacks || jetpacks.length < 1) {
                output = "You haven't installed any Jetpacks. Run '> jetpack create' to get going.";
            } else {
                output = "<u>Your Jetpack Features</u><br/><br/>";

                output += dojo.map(dojo.filter(jetpacks, function(file) {
                    return bespin.util.endsWith(file.name, '\\.js');
                }), function(c) {
                    return "<a href=\"javascript:bespin.get('commandLine').executeCommand('open " + c.name + " " + bespin.jetpack.projectName + "');\">" + c.name.replace(/\.js$/, '') + "</a>";
                }).join("<br>");
            }

            instruction.addOutput(output);
        });
    }
});

/**
 * 'jetpack edit' command
 */
bespin.jetpack.commands.addCommand({
    name: 'edit',
    takes: ['feature'],
    preview: 'edit the given Jetpack feature',
    completeText: 'feature name to edit (required)',
    usage: '[feature]: feature name required.',
    execute: function(instruction, feature) {
        if (!feature) {
            bespin.get("commandLine").showUsage(this);
            return;
        }

        var path = feature + '.js';

        bespin.get('files').whenFileExists(bespin.jetpack.projectName, path, {
            execute: function() {
                bespin.publish("editor:openfile", {
                    project: bespin.jetpack.projectName,
                    filename: path
                });
            },
            elseFailed: function() {
                bespin.get("commandLine").addErrorOutput("No feature called " + feature + ".<br><br><em>Run 'jetpack list' to see what is available.</em>");
            }
        });
    }
});

/**
 * Jetpack setting to turn off the toolbar icon if set to off
 * <p>If you "set jetpack on", wire up the toolbar to have the jetpack icon
 */
bespin.subscribe("settings:set:jetpack", function(event) {
    var newset = bespin.get("settings").isOff(event.value);
    var jptb = dojo.byId('toolbar_jetpack');

    if (newset) { // turn it off
        if (jptb) jptb.style.display = 'none';
    } else { // turn it on
        if (jptb) {
            jptb.style.display = 'inline';
        } else {
            // <img id="toolbar_jetpack" src="images/icn_jetpack.png" alt="Jetpack" style="padding-left: 10px;" title="Jetpack (show or hide menu)">
            dojo.byId('toolbar').appendChild(dojo.create("img", {
               id: "toolbar_jetpack",
               src: "images/icn_jetpack.png",
               alt: "Jetpack",
               style: "padding-left: 10px",
               title: "Jetpack (show or hide menu)"
            }));

            // wire up the toolbar fun
            bespin.get("toolbar").setup("jetpack", "toolbar_jetpack");
        }
    }
});

/**
 * Jetpack toolbar item
 */
bespin.subscribe("toolbar:init", function(event) {
    event.toolbar.addComponent('jetpack', function(toolbar, el) {
        var jetpack = dojo.byId(el) || dojo.byId("toolbar_jetpack");

        var highlightOn = function() {
            jetpack.src = "images/icn_jetpack_on.png";
        };

        var highlightOff = function() {
            jetpack.src = "images/icn_jetpack.png";
        };

        dojo.connect(jetpack, 'mouseover', highlightOn);
        dojo.connect(jetpack, 'mouseout',  function() {
            var dropdown = dojo.byId('jetpack_dropdown');
            if (!dropdown || dropdown.style.display == 'none') highlightOff();
        });

        // Change the font size between the small, medium, and large settings
        dojo.connect(jetpack, 'click', function() {
            var dropdown = dojo.byId('jetpack_dropdown');

            if (!dropdown || dropdown.style.display == 'none') { // no dropdown or hidden, so show
                highlightOn();

                dropdown = dropdown || (function() {
                    var dd = dojo.create("div", {
                        id: 'jetpack_dropdown'
                    });

                    var editor_coords = dojo.coords('editor');
                    var jetpack_coorders = dojo.coords(jetpack);
                    dojo.style(dd, {
                        position: 'absolute',
                        padding: '0px',
                        top: editor_coords.y + 'px',
                        left: (jetpack_coorders.x - 30) + 'px',
                        display: 'none',
                        zIndex: '150'
                    });

                    dd.innerHTML = '<table id="jetpack_dropdown_content"><tr><th colspan="3">Jetpack Actions</th></tr><tr><td>create</td><td><input type="text" size="7" id="jetpack_dropdown_input_create" value="myjetpack" onfocus="bespin.get(\'editor\').setFocus(false);"></td><td><input id="jetpack_dropdown_now_create" type="button" value="now &raquo;"></td></tr><tr id="jetpack_dropdown_or"><td colspan="3" align="center">or</td></tr><tr><td>install</td><td><select id="jetpack_dropdown_input_install"><option></option></select></td><td><input id="jetpack_dropdown_now_install" type="button" value="now &raquo;"></td></tr></table><div id="jetpack_dropdown_border">&nbsp;</div>';

                    document.body.appendChild(dd);

                    // render out of view to get the size info and then hide again
                    bespin.jetpack.sizeDropDownBorder(dd);

                    var cl = bespin.get("commandLine");
                    // create a new jetpack
                    dojo.connect(dojo.byId('jetpack_dropdown_now_create'), 'click', function() {
                        cl.executeCommand('jetpack create ' + dojo.byId('jetpack_dropdown_input_create').value);
                        dropdown.style.display = 'none';
                    });

                    // install a jetpack
                    dojo.connect(dojo.byId('jetpack_dropdown_now_install'), 'click', function() {
                        cl.executeCommand('jetpack install ' + dojo.byId('jetpack_dropdown_input_install').value);
                        dropdown.style.display = 'none';
                    });

                    return dd;
                })();

                dropdown.style.display = 'block';

                bespin.jetpack.loadInstallScripts();
            } else { // hide away
                highlightOff();

                dropdown.style.display = 'none';
            }
        });
    });
});

/**
 * Install a jetpack
 */
bespin.jetpack.install = function(feature) {
    // add the link tag to the body
    // <link rel="jetpack" href="path/feature.js">
    var link = dojo.create("link", {
        rel: 'jetpack',
        href: bespin.util.path.combine("preview/at", bespin.jetpack.projectName, feature + ".js"),
        name: feature
    }, dojo.body());

    // Use the custom event to install
    // var event = document.createEvent("Events");
    // var element = dojo.byId("jetpackInstallEvent");
    //
    // // create a jetpack event element if it doesn't exist.
    // if (!element) {
    //     element = dojo.create("div", {
    //        id: "jetpackEvent",
    //     }, dojo.body());
    //     element.setAttribute("hidden", true);
    // }
    //
    // // set the code string to the "mozjpcode" attribute.
    // element.setAttribute("mozjpcode", bespin.get("editor").model.getDocument());
    //
    // // init and dispatch the event.
    // event.initEvent("mozjpinstall", true, false);
    // element.dispatchEvent(event);
};

/**
 * Utility to get the size of the drop-down border
 */
bespin.jetpack.sizeDropDownBorder = function(dd) {
    var keephidden = false;
    if (dd) {
        keephidden = true;
    } else {
        dd = dojo.byId('jetpack_dropdown');
    }

    if (keephidden) {
        dd.style.right = '-50000px';
        dd.style.display = 'block';
    }

    var content_coords = dojo.coords('jetpack_dropdown_content');

    if (keephidden) {
        dd.style.right = '';
        dd.style.display = 'none';
    }

    dojo.style('jetpack_dropdown_border', {
        width: content_coords.w + 'px',
        height: content_coords.h + 'px'
    });
};

/**
 * Utility to load the installation scripts
 */
bespin.jetpack.loadInstallScripts = function() {
    bespin.get('server').list(bespin.jetpack.projectName, '', function(jetpacks) {
        var output;

        if (jetpacks && jetpacks.length > 0) {
            output += dojo.map(dojo.filter(jetpacks, function(file) {
                return bespin.util.endsWith(file.name, '\\.js');
            }), function(c) {
                return "<option>" + c.name.replace(/\.js$/, '') + "</option>";
            }).join("");
        }

        dojo.byId("jetpack_dropdown_input_install").innerHTML = output;
        bespin.jetpack.sizeDropDownBorder();
    });
};
