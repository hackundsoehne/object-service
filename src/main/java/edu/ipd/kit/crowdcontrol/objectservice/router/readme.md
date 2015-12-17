## Api

Method  | URI	 											    | Description                                                     |
-------:|:------------------------------------------------------|:----------------------------------------------------------------|
GET		| /														| Lists all endpoints, used for checking authentication.
GET     | /experiments                                          | List all experiments.
PUT		| /experiments											| Creates a new experiment.
GET		| /experiments/**{id}**									| Lists all properties of a given experiment.
PATCH 	| /experiments/**{id}**									| Edits the given properties of an experiment.
DELETE  | /experiments/**{id}**									| Deletes an experiment and all results permanently.
POST 	| /experiments/**{id}**/start							| Starts an experiment which must already be configured.
POST 	| /experiments/**{id}**/creative_stop					| stops the experiment from getting creative answers
POST 	| /experiments/**{id}**/stop							| stops the experiment from beeing public

PUT		| /answer											    | Creates a new answer
GET		| /answer/**{id}**									    | Lists all properties of a given answer.
PUT     | /answer/**{id}**/rate                                 | Create a rating for a answer will rateunlock this answer
PUT     | /answers/ratelock                                     | passes a list of answers to ratelock

PUT		| /worker											    | Creates a worker with emailadress / updates the emailadress
GET		| /worker/**{id}**									    | List infos of the worker
DELETE  | /worker/**{id}**                                      | deletes the user. References to this user will be replaced by (annonym|a hash without known mailadress)