/**
 * Creates a pre-compiled Hogan template
 *
 * @return a pre-compiled Hogan template
 * @since 1.0-SNAPSHOT
 */
const createTemplate = () => {
    const template = `
      <article class='slacker'>
        <span>
          <img class='avatar' src='{{avatar}}' />
        </span>
        <span class='name'>{{name}}</span>
        <span class='order'>{{noCommits}}</span>
        <span class='rate'>{{rate}}%</span>
        <span class='behind'>{{lastCommitAt}}</span>
      </article>
    `
    return Hogan.compile(template)
}

/**
 * Renders all slackers coming from API via a pre-compiled Hogan
 * template
 *
 * @param slackerList the list of the slackers coming from API
 * @since 1.0-SNAPSHOT
 */
const renderSlackers = (slackers) => {
    const htmlListContainer = document.querySelector('#slackerList');
    const template = createTemplate()
    const htmlListElements = slackers.map(slacker => template.render(slacker));

    htmlListContainer.innerHTML = htmlListElements.join('')
}


const updateDate = (commiterList) => {
    return commiterList.map(commiter => {
        const lastCommit = commiter['lastCommitAt'];
        const parsedDate = moment(lastCommit, 'YYYY-MM-DD').fromNow()

        commiter['lastCommitAt'] = parsedDate

        console.log('lastCommit: ', lastCommit, ' -- parsedDate: ', parsedDate)
        return commiter
    })
}

/**
 * Retrieve slackers from server API and then renders them
 *
 * @return a promise containing the execution of rendering
 * slackers coming from the API
 * @since 1.0-SNAPSHOT
 */
const fetchSlackers = () => {
    return fetch('http://localhost:5050/slackers')
        .then(resp => resp.json())
        .then(updateDate)
        .then(json => renderSlackers(json))
}

fetchSlackers()
