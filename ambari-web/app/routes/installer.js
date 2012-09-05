/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

module.exports = Em.Route.extend({
  route: '/installer',

  enter: function (router) {
    console.log('in /installer:enter');

    if (router.getAuthenticated()) {
      Ember.run.next(function () {
        router.transitionTo('step' + router.getInstallerCurrentStep());
      });
    } else {
      Ember.run.next(function () {
        router.transitionTo('login');
      });
    }
  },

  connectOutlets: function (router, context) {
    console.log('in /installer:connectOutlets');
    router.get('applicationController').connectOutlet('installer');
  },

  step1: Em.Route.extend({
    route: '/step1',
    connectOutlets: function (router, context) {
      console.log('in installer.step1:connectOutlets');
      router.setInstallerCurrentStep('1', false);
      router.get('installerController').connectOutlet('installerStep1');
    },
    next: Em.Router.transitionTo('step2')
  }),

  step2: Em.Route.extend({
    route: '/step2',
    connectOutlets: function (router, context) {
      router.setInstallerCurrentStep('2', false);
      router.get('installerController').connectOutlet('installerStep2');
    },
    back: Em.Router.transitionTo('step1'),
    next: Em.Router.transitionTo('step3')
  }),

  step3: Em.Route.extend({
    route: '/step3',
    connectOutlets: function (router, context) {
      router.setInstallerCurrentStep('3', false);
      router.get('installerController').connectOutlet('installerStep3');
    },
    back: Em.Router.transitionTo('step2'),
    next: Em.Router.transitionTo('step4')
  }),

  step4: Em.Route.extend({
    route: '/step4',
    connectOutlets: function (router, context) {
      router.setInstallerCurrentStep('4', false);
      router.get('installerController').connectOutlet('installerStep4');
    },
    back: Em.Router.transitionTo('step3'),
    next: Em.Router.transitionTo('step5')
  }),

  step5: Em.Route.extend({
    route: '/step5',
    connectOutlets: function (router, context) {
      router.setInstallerCurrentStep('5', false);
      router.get('installerController').connectOutlet('installerStep5');
    },
    back: Em.Router.transitionTo('step4'),
    next: Em.Router.transitionTo('step6')
  }),

  step6: Em.Route.extend({
    route: '/step6',
    connectOutlets: function (router, context) {
      router.setInstallerCurrentStep('6', false);
      router.get('installerController').connectOutlet('installerStep6');
    },
    back: Em.Router.transitionTo('step5'),
    next: Em.Router.transitionTo('step7')
  }),

  step7: Em.Route.extend({
    route: '/step7',
    connectOutlets: function (router, context) {
      router.setInstallerCurrentStep('7', false);
      router.get('installerController').connectOutlet('installerStep7');
    },
    back: Em.Router.transitionTo('step6'),
    next: Em.Router.transitionTo('step8')
  }),

  step8: Em.Route.extend({
    route: '/step8',
    connectOutlets: function (router, context) {
      router.setInstallerCurrentStep('8', false);
      router.get('installerController').connectOutlet('installerStep8');
    },

    back: Em.Router.transitionTo('step7'),

    complete: function (router, context) {
      if (true) {   // this function will be moved to installerController where it will validate
        router.setInstallerCurrentStep('1', true);
        router.setSection('main');
        router.transitionTo('main');
      } else {
        console.log('cluster installation failure');
      }
    }
  }),

  gotoStep1: Em.Router.transitionTo('step1'),

  gotoStep2: Em.Router.transitionTo('step2'),

  gotoStep3: Em.Router.transitionTo('step3'),

  gotoStep4: Em.Router.transitionTo('step4'),

  gotoStep5: Em.Router.transitionTo('step5'),

  gotoStep6: Em.Router.transitionTo('step6'),

  gotoStep7: Em.Router.transitionTo('step7'),

  gotoStep8: Em.Router.transitionTo('step8')

});