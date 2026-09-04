import EnvironmentBuilder from './node_modules/generator-jhipster/dist/cli/environment-builder.js';

async function main() {
  console.log('Starting JDL import programmatically...');
  await EnvironmentBuilder.run(['jhipster:jdl', '../fotolou.jdl'], {
    force: true,
    skipChecks: true,
    noInsight: true,
    skipInstall: true,
  });
  console.log('JDL import finished successfully!');
}

main().catch(err => {
  console.error('Error running JDL import:', err);
  process.exit(1);
});
