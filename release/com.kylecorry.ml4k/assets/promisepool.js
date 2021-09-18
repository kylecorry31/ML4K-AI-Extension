/**
 * Like `Promise.all` but you can specify how many concurrent tasks you want at once.
 */
async function pool({
  collection,
  task,
  maxConcurrency
}) {
  if (!maxConcurrency) {
    return Promise.all(collection.map((item, i) => task(item, i)));
  }

  if (!collection.length) {
    return [];
  }

  const results = [];
  const mutableCollection = collection.slice().map((t, i) => [t, i]);
  let available = maxConcurrency;
  let done = false;
  let globalResolve;
  let globalReject;
  const finalPromise = new Promise((resolve, reject) => {
    globalResolve = resolve;
    globalReject = reject;
  });
  const listeners = new Set();

  function notify() {
    for (const listener of listeners) {
      listener();
    }
  }

  function ready() {
    return new Promise(resolve => {
      const listener = () => {
        if (done) {
          listeners.delete(listener);
          resolve();
        } else if (available > 0) {
          listeners.delete(listener);
          available -= 1;
          resolve();
        }
      };

      listeners.add(listener);
      notify();
    });
  }

  while (true) {
    const value = mutableCollection.shift();
    if (!value) break;
    if (done) break;
    const [t, i] = value;
    await ready();
    task(t, i).then(r => {
      results.push([r, i]);
      available += 1;

      if (results.length === collection.length) {
        done = true;
        globalResolve();
      }
    }).catch(e => {
      done = true;
      globalReject(e);
    }).finally(notify);
  }

  await finalPromise;
  return results.slice().sort(([, a], [, b]) => a - b).map(([r]) => r);
}

module.exports = pool;
//# sourceMappingURL=index.js.map
